package com.tuituidan.openhub.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;
import com.tuituidan.openhub.bean.dto.PostTableData;
import com.tuituidan.openhub.bean.dto.SysAppHeaderParam;
import com.tuituidan.openhub.bean.entity.SysDataLog;
import com.tuituidan.openhub.bean.entity.SysPushLog;
import com.tuituidan.openhub.bean.vo.DataConfigView;
import com.tuituidan.openhub.bean.vo.SysAppView;
import com.tuituidan.openhub.bean.vo.TableStruct;
import com.tuituidan.openhub.consts.enums.DataChangeEnum;
import com.tuituidan.openhub.consts.enums.PushStatusEnum;
import com.tuituidan.openhub.mapper.SysDataLogMapper;
import com.tuituidan.openhub.mapper.SysPushLogMapper;
import com.tuituidan.tresdin.mybatis.util.SnowFlake;
import com.tuituidan.tresdin.util.ExpParserUtils;
import com.tuituidan.tresdin.util.StringExtUtils;
import com.tuituidan.tresdin.util.thread.CompletableUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.RequestEntity.BodyBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
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
    public void push(DataConfigView configView, DataChangeEnum type,
            List<JSONObject> dataList, List<SysAppView> appList) {
        PostTableData postData = buildPostTableData(configView, type, dataList);
        Long dataLogId = createSysDataLog(configView, type, dataList);
        if (appList.size() == 1) {
            pushToApp(dataLogId, appList.get(0), postData);
            return;
        }
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (SysAppView sysApp : appList) {
            futures.add(CompletableUtils.runAsync(() -> pushToApp(dataLogId, sysApp, postData), "data-push")
                    .exceptionally(ex -> {
                        log.error("数据日志推送异常", ex);
                        return null;
                    }));
        }
        CompletableUtils.waitAll(futures);
    }

    /**
     * push
     *
     * @param configView configView
     * @param pushLog pushLog
     * @param dataList dataList
     * @param sysApp sysApp
     */
    public void push(DataConfigView configView, SysPushLog pushLog,
            List<JSONObject> dataList, SysAppView sysApp) {
        PostTableData postData = buildPostTableData(configView, DataChangeEnum.REPLACE, dataList);
        pushToApp(pushLog, sysApp, postData);
    }

    private Long createSysDataLog(DataConfigView configView, DataChangeEnum type,
            List<JSONObject> dataList) {
        SysDataLog dataLog = new SysDataLog()
                .setId(SnowFlake.newId())
                .setDatasourceId(configView.getDatasourceId())
                .setDatabaseConfigId(configView.getId())
                .setDatabaseName(configView.getDatabaseName())
                .setTableName(configView.getTableName())
                .setPrimaryKey(configView.getPrimaryKey())
                .setOperType(type.getCode())
                .setDataLog(extractRecordLogList(configView.getRecordColumn(), dataList));
        sysDataLogMapper.insert(dataLog);
        return dataLog.getId();
    }

    private String extractRecordLogList(String[] recordColumn, List<JSONObject> dataList) {
        if (ArrayUtils.isEmpty(recordColumn)) {
            return JSONObject.toJSONString(dataList);
        }
        List<JSONObject> resultList = new ArrayList<>();
        for (JSONObject source : dataList) {
            JSONObject target = new JSONObject();
            for (String column : recordColumn) {
                target.put(column, source.get(column));
            }
            resultList.add(target);
        }
        return JSONObject.toJSONString(resultList);
    }

    private PostTableData buildPostTableData(DataConfigView configView, DataChangeEnum type,
            List<JSONObject> dataList) {
        return new PostTableData().setDataList(dataList)
                .setTable(configView.getTableName())
                .setDatabase(configView.getDatabaseName())
                .setType(type.getCode())
                .setColumns(configView.getTableStruct().stream()
                        .map(TableStruct::getColumnName).collect(Collectors.toList()))
                .setPrimaryKey(configView.getPrimaryKey());
    }

    private void pushToApp(Long dataLogId, SysAppView sysApp, PostTableData postData) {
        SysPushLog pushLog = new SysPushLog();
        long startTime = System.currentTimeMillis();
        pushLog.setAppId(sysApp.getId())
                .setPushTime(LocalDateTime.now())
                .setDataLogId(dataLogId);
        pushToApp(pushLog, sysApp, postData);
        pushLog.setCostTime(System.currentTimeMillis() - startTime);
        pushLog.setPushTimes(1);
        sysPushLogMapper.insert(pushLog);
    }

    private void pushToApp(SysPushLog pushLog, SysAppView sysApp, PostTableData postData) {
        BodyBuilder bodyBuilder = RequestEntity.post(sysApp.getUrl())
                .acceptCharset(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON);
        addPushRequestHeader(bodyBuilder, sysApp);
        try {
            ResponseEntity<String> response = restTemplate.exchange(bodyBuilder.body(postData), String.class);
            pushLog.setResponse(StringUtils.truncate(StringUtils.defaultString(response.getBody(),
                    "无数据返回"), 4000));
            pushLog.setStatus(analysePushStatus(response, sysApp));
        } catch (Exception ex) {
            pushLog.setResponse(StringUtils.truncate(ExceptionUtils.getStackTrace(ex), 4000));
            pushLog.setStatus(PushStatusEnum.FAIL.getCode());
        }
    }

    private void addPushRequestHeader(BodyBuilder bodyBuilder, SysAppView sysApp) {
        if (CollectionUtils.isEmpty(sysApp.getHeaders())) {
            return;
        }
        for (SysAppHeaderParam header : sysApp.getHeaders()) {
            if ("key-value".equals(header.getType())) {
                bodyBuilder.header(header.getKey(), header.getValue());
            } else if ("basic".equals(header.getType())) {
                bodyBuilder.header("Authorization", buildBasicToken(header));
            } else if ("bearer".equals(header.getType())) {
                bodyBuilder.header("Authorization", buildBearerToken(sysApp));
            }
        }
    }

    private String analysePushStatus(ResponseEntity<String> response, SysAppView sysApp) {
        if (StringUtils.isBlank(sysApp.getResultExp())) {
            return response.getStatusCode().is2xxSuccessful()
                    ? PushStatusEnum.SUCCESS.getCode() : PushStatusEnum.FAIL.getCode();

        }
        if (StringUtils.isBlank(response.getBody())) {
            return PushStatusEnum.FAIL.getCode();
        }
        return ExpParserUtils.evaluate(sysApp.getResultExp(), JSON.parseObject(response.getBody()))
                ? PushStatusEnum.SUCCESS.getCode() : PushStatusEnum.FAIL.getCode();
    }

    private String buildBasicToken(SysAppHeaderParam header) {
        return StringExtUtils.format("Basic {}",
                Base64Utils.encodeToUrlSafeString(StringExtUtils.format("{}:{}",
                        header.getKey(), header.getValue()).getBytes(StandardCharsets.UTF_8)));
    }

    private String buildBearerToken(SysAppView sysApp) {
        return "Bearer " + appTokenCache.get(sysApp.getAppKey(), key -> Jwts.builder().setSubject(key)
                .signWith(SignatureAlgorithm.HS512, sysApp.getAppSecret())
                .setExpiration(DateUtils.addMinutes(new Date(), 30))
                .compact());
    }

}
