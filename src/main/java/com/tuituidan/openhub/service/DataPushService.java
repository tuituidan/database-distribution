package com.tuituidan.openhub.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;
import com.tuituidan.openhub.bean.dto.PostTableData;
import com.tuituidan.openhub.bean.entity.SysApp;
import com.tuituidan.openhub.bean.entity.SysDataLog;
import com.tuituidan.openhub.bean.entity.SysPushLog;
import com.tuituidan.openhub.bean.vo.SysDatabaseConfigView;
import com.tuituidan.openhub.bean.vo.TableStruct;
import com.tuituidan.openhub.consts.enums.DataChangeEnum;
import com.tuituidan.openhub.consts.enums.PushStatusEnum;
import com.tuituidan.openhub.mapper.SysDataLogMapper;
import com.tuituidan.openhub.mapper.SysPushLogMapper;
import com.tuituidan.tresdin.mybatis.util.SnowFlake;
import com.tuituidan.tresdin.util.ExpParserUtils;
import com.tuituidan.tresdin.util.thread.CompletableUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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
    public void push(SysDatabaseConfigView configView, DataChangeEnum type,
            List<Map<String, Object>> dataList, List<SysApp> appList) {
        PostTableData postData = buildPostTableData(configView, type, dataList);
        Long dataLogId = createSysDataLog(configView, type, dataList);
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (SysApp sysApp : appList) {
            futures.add(CompletableUtils.runAsync(() -> pushToApp(dataLogId, sysApp, postData)));
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
    public void push(SysDatabaseConfigView configView, SysPushLog pushLog,
            List<Map<String, Object>> dataList, SysApp sysApp) {
        PostTableData postData = buildPostTableData(configView, DataChangeEnum.REPLACE, dataList);
        pushToApp(pushLog, sysApp, postData);
    }

    private Long createSysDataLog(SysDatabaseConfigView configView, DataChangeEnum type,
            List<Map<String, Object>> dataList) {
        SysDataLog dataLog = new SysDataLog()
                .setId(SnowFlake.newId())
                .setDatasourceId(configView.getDatasourceId())
                .setDatabaseConfigId(configView.getId())
                .setDatabaseName(configView.getDatabaseName())
                .setTableName(configView.getTableName())
                .setPrimaryKey(configView.getPrimaryKey())
                .setOperType(type.getCode())
                .setDataLog(JSONObject.toJSONString(dataList));
        sysDataLogMapper.insert(dataLog);
        return dataLog.getId();
    }

    private PostTableData buildPostTableData(SysDatabaseConfigView configView, DataChangeEnum type,
            List<Map<String, Object>> dataList) {
        return new PostTableData().setDataList(dataList)
                .setTable(configView.getTableName())
                .setDatabase(configView.getDatabaseName())
                .setType(type.getCode())
                .setColumns(configView.getTableStruct().stream()
                        .map(TableStruct::getColumnName).collect(Collectors.toList()))
                .setPrimaryKey(configView.getPrimaryKey());
    }

    private void pushToApp(Long dataLogId, SysApp sysApp, PostTableData postData) {
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

    private void pushToApp(SysPushLog pushLog, SysApp sysApp, PostTableData postData) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(RequestEntity.post(sysApp.getUrl())
                    .header("AppKey", sysApp.getAppKey())
                    .header("Authorization", getToken(sysApp))
                    .acceptCharset(StandardCharsets.UTF_8)
                    .contentType(MediaType.APPLICATION_JSON).body(postData), String.class);
            pushLog.setResponse(StringUtils.truncate(StringUtils.defaultString(response.getBody(),
                    "无数据返回"), 4000));
            pushLog.setStatus(analysePushStatus(response, sysApp));
        } catch (Exception ex) {
            pushLog.setResponse(StringUtils.truncate(ExceptionUtils.getStackTrace(ex), 4000));
            pushLog.setStatus(PushStatusEnum.FAIL.getCode());
        }
    }

    private String analysePushStatus(ResponseEntity<String> response, SysApp sysApp) {
        if (StringUtils.isBlank(sysApp.getResultExp())) {
            return response.getStatusCodeValue() == HttpStatus.OK.value()
                    ? PushStatusEnum.SUCCESS.getCode() : PushStatusEnum.FAIL.getCode();

        }
        if (StringUtils.isBlank(response.getBody())) {
            return PushStatusEnum.FAIL.getCode();
        }
        return ExpParserUtils.evaluate(sysApp.getResultExp(), JSON.parseObject(response.getBody()))
                ? PushStatusEnum.SUCCESS.getCode() : PushStatusEnum.FAIL.getCode();
    }

    private String getToken(SysApp sysApp) {
        return appTokenCache.get(sysApp.getAppKey(), key -> Jwts.builder().setSubject(key)
                .signWith(SignatureAlgorithm.HS512, sysApp.getAppSecret())
                .setExpiration(DateUtils.addMinutes(new Date(), 30))
                .compact());
    }

}
