package com.tuituidan.openhub.service;

import com.alibaba.fastjson2.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;
import com.tuituidan.openhub.bean.dto.PostTableData;
import com.tuituidan.openhub.bean.entity.SysApp;
import com.tuituidan.openhub.bean.entity.SysAppDatabaseConfig;
import com.tuituidan.openhub.bean.vo.SysDatabaseConfigView;
import com.tuituidan.openhub.bean.vo.TableStruct;
import com.tuituidan.openhub.mapper.SysAppDatabaseConfigMapper;
import com.tuituidan.openhub.mapper.SysAppMapper;
import com.tuituidan.tresdin.util.thread.CompletableUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
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
public class DataPushService implements ApplicationRunner {

    @Resource
    private SysAppDatabaseConfigMapper sysAppDatabaseConfigMapper;

    @Resource
    private SysAppMapper sysAppMapper;

    private final Map<Long, List<SysApp>> databaseAppConfigMap = new HashMap<>();

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private Cache<String, String> appTokenCache;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<Long, SysApp> appMap = sysAppMapper.selectAll().stream().collect(Collectors.toMap(SysApp::getId,
                Function.identity()));
        List<SysAppDatabaseConfig> configList = sysAppDatabaseConfigMapper.selectAll();
        for (SysAppDatabaseConfig config : configList) {
            List<SysApp> list = databaseAppConfigMap.getOrDefault(config.getDatabaseConfigId(),
                    new ArrayList<>());
            list.add(appMap.get(config.getAppId()));
            databaseAppConfigMap.put(config.getDatabaseConfigId(), list);
        }
    }

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
            PostTableData postData = new PostTableData().setDataList(datas)
                    .setTable(configView.getTableName())
                    .setDatabase(configView.getDatabaseName())
                    .setType(type)
                    .setColumns(configView.getTableStruct().stream()
                            .map(TableStruct::getColumnName).collect(Collectors.toList()))
                    .setPrimaryKey(configView.getPrimaryKey());
            System.out.println(JSONObject.toJSONString(postData));
            push(configView, postData);
        });
    }

    private void push(SysDatabaseConfigView configView, PostTableData postData) {
        List<SysApp> sysApps = databaseAppConfigMap.get(configView.getId());
        for (SysApp sysApp : sysApps) {
            CompletableUtils.runAsync(() -> {
                ResponseEntity<String> response = restTemplate.exchange(RequestEntity.post(sysApp.getUrl())
                        .header("appkey", sysApp.getAppKey())
                        .header("Authorization", getToken(sysApp))
                        .acceptCharset(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON).body(postData), String.class);
                System.out.println("发送结果：" + JSONObject.toJSONString(response.getBody()));
            });
        }
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