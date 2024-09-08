package com.tuituidan.openhub.service;

import com.alibaba.fastjson2.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;
import com.tuituidan.openhub.bean.dto.PostTableData;
import com.tuituidan.openhub.bean.entity.SysApp;
import com.tuituidan.openhub.bean.entity.SysDataLog;
import com.tuituidan.openhub.bean.entity.SysPushLog;
import com.tuituidan.openhub.bean.vo.SysDatabaseConfigView;
import com.tuituidan.openhub.bean.vo.TableStruct;
import com.tuituidan.openhub.mapper.SysDataLogMapper;
import com.tuituidan.openhub.mapper.SysPushLogMapper;
import com.tuituidan.tresdin.mybatis.util.SnowFlake;
import com.tuituidan.tresdin.util.thread.CompletableUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * DataPushService.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/1
 */
@Service
@Slf4j
public class DataPushService {

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private Cache<String, String> appTokenCache;

    @Resource
    private SysDataLogMapper sysDataLogMapper;

    @Resource
    private SysPushLogMapper sysPushLogMapper;

    /**
     * analyse
     *
     * @param configView configView
     * @param type type
     * @param rows rows
     */
    public void analyse(SysDatabaseConfigView configView, String type, List<Serializable[]> rows) {
        CompletableUtils.runAsync(() -> {
            List<JSONObject> datas = buildTable(configView.getTableStruct(), rows);
            SysDataLog dataLog = new SysDataLog()
                    .setId(SnowFlake.newId())
                    .setDatasourceId(configView.getDatasourceId())
                    .setDatabaseConfigId(configView.getId())
                    .setDatabaseName(configView.getDatabaseName())
                    .setTableName(configView.getTableName())
                    .setPrimaryKey(configView.getPrimaryKey())
                    .setOperType(type)
                    .setDataLog(JSONObject.toJSONString(datas));
            sysDataLogMapper.insert(dataLog);
            pushToApps(dataLog, configView, datas);
        }).exceptionally(ex -> {
            log.error("数据日志解析异常", ex);
            return null;
        });
    }

    private void pushToApps(SysDataLog dataLog, SysDatabaseConfigView configView, List<JSONObject> datas) {
        PostTableData postData = new PostTableData().setDataList(datas)
                .setTable(dataLog.getTableName())
                .setDatabase(dataLog.getDatabaseName())
                .setType(dataLog.getOperType())
                .setColumns(configView.getTableStruct().stream()
                        .map(TableStruct::getColumnName).collect(Collectors.toList()))
                .setPrimaryKey(dataLog.getPrimaryKey());
        for (SysApp sysApp : configView.getAppList()) {
            CompletableUtils.runAsync(() -> pushToApp(sysApp, dataLog, postData));
        }
    }

    private void pushToApp(SysApp sysApp, SysDataLog dataLog, PostTableData postData) {
        SysPushLog pushLog = new SysPushLog();
        long startTime = System.currentTimeMillis();
        pushLog.setAppId(sysApp.getId())
                .setPushTime(LocalDateTime.now())
                .setDataLogId(dataLog.getId());
        try {
            ResponseEntity<String> response = restTemplate.exchange(RequestEntity.post(sysApp.getUrl())
                    .header("AppKey", sysApp.getAppKey())
                    .header("Authorization", getToken(sysApp))
                    .acceptCharset(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON).body(postData), String.class);
            pushLog.setResponse(StringUtils.defaultString(response.getBody(), "无数据返回"));
            pushLog.setStatus(Objects.toString(response.getStatusCodeValue()));
        } catch (HttpServerErrorException ex) {
            pushLog.setResponse(StringUtils.truncate(ExceptionUtils.getStackTrace(ex), 4000));
            pushLog.setStatus(Objects.toString(ex.getStatusCode().value()));
        } catch (Exception ex) {
            pushLog.setResponse("未知异常：" + StringUtils.truncate(ExceptionUtils.getStackTrace(ex), 3990));
            pushLog.setStatus(Objects.toString(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
        pushLog.setCostTime(System.currentTimeMillis() - startTime);
        sysPushLogMapper.insert(pushLog);
    }

    private String getToken(SysApp sysApp) {
        return appTokenCache.get(sysApp.getAppKey(), key -> Jwts.builder().setSubject(key)
                .signWith(SignatureAlgorithm.HS512, sysApp.getAppSecret())
                .setExpiration(DateUtils.addMinutes(new Date(), 30))
                .compact());
    }

    private List<JSONObject> buildTable(List<TableStruct> tableStructs, List<Serializable[]> rows) {
        List<JSONObject> list = new ArrayList<>();
        for (Serializable[] row : rows) {
            JSONObject item = new JSONObject();
            for (TableStruct struct : tableStructs) {
                item.put(struct.getColumnName(), formatData(row[struct.getOrdinalPosition() - 1]));
            }
            list.add(item);
        }
        return list;
    }

    private Object formatData(Object data) {
        if (data instanceof byte[]) {
            try {
                return IOUtils.toString(new ByteArrayInputStream((byte[]) data), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalArgumentException("数据转换错误", e);
            }
        }
        if (data instanceof BitSet) {
            return ((BitSet) data).get(0);
        }
        if (data instanceof Timestamp) {
            return ((Timestamp) data).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (data instanceof java.sql.Date || data instanceof java.sql.Time) {
            return data.toString();
        }
        if (data instanceof Date) {
            return DateFormatUtils.format((Date) data, "yyyy-MM-dd HH:mm:ss");
        }
        return data;
    }

}
