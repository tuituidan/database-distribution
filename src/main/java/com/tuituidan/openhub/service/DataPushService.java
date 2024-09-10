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
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
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
     * push
     *
     * @param configView configView
     * @param type type
     * @param dataList dataList
     */
    public void push(SysDatabaseConfigView configView, String type, List<Map<String, Object>> dataList) {
        PostTableData postData = new PostTableData().setDataList(dataList)
                .setTable(configView.getTableName())
                .setDatabase(configView.getDatabaseName())
                .setType(type)
                .setColumns(configView.getTableStruct().stream()
                        .map(TableStruct::getColumnName).collect(Collectors.toList()))
                .setPrimaryKey(configView.getPrimaryKey());
        Long dataLogId = createSysDataLog(configView, type, dataList);
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (SysApp sysApp : configView.getAppList()) {
            futures.add(CompletableUtils.runAsync(() -> pushToApp(sysApp, dataLogId, postData)));
        }
        CompletableUtils.waitAll(futures);
    }

    private Long createSysDataLog(SysDatabaseConfigView configView, String type,
            List<Map<String, Object>> dataList) {
        SysDataLog dataLog = new SysDataLog()
                .setId(SnowFlake.newId())
                .setDatasourceId(configView.getDatasourceId())
                .setDatabaseConfigId(configView.getId())
                .setDatabaseName(configView.getDatabaseName())
                .setTableName(configView.getTableName())
                .setPrimaryKey(configView.getPrimaryKey())
                .setOperType(type)
                .setDataLog(JSONObject.toJSONString(dataList));
        sysDataLogMapper.insert(dataLog);
        return dataLog.getId();
    }

    private void pushToApp(SysApp sysApp, Long dataLogId, PostTableData postData) {
        SysPushLog pushLog = new SysPushLog();
        long startTime = System.currentTimeMillis();
        pushLog.setAppId(sysApp.getId())
                .setPushTime(LocalDateTime.now())
                .setDataLogId(dataLogId);
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

}
