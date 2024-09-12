package com.tuituidan.openhub.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.tuituidan.openhub.bean.entity.SysApp;
import com.tuituidan.openhub.bean.entity.SysDataLog;
import com.tuituidan.openhub.bean.entity.SysDatabaseConfig;
import com.tuituidan.openhub.bean.entity.SysPushLog;
import com.tuituidan.openhub.bean.vo.SysDatabaseConfigView;
import com.tuituidan.openhub.bean.vo.TableStruct;
import com.tuituidan.openhub.config.AppPropertiesConfig;
import com.tuituidan.openhub.consts.Consts;
import com.tuituidan.openhub.consts.enums.DataChangeEnum;
import com.tuituidan.openhub.consts.enums.IncrementTypeEnum;
import com.tuituidan.openhub.mapper.SysDatabaseConfigMapper;
import com.tuituidan.tresdin.util.BeanExtUtils;
import com.tuituidan.tresdin.util.StringExtUtils;
import com.tuituidan.tresdin.util.thread.CompletableUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * DataAnalyseService.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/12
 */
@Service
@Slf4j
public class DataAnalyseService {

    @Resource
    private SysDatabaseConfigMapper sysDatabaseConfigMapper;

    @Resource
    private AppPropertiesConfig appPropertiesConfig;

    @Resource
    private DataPushService dataPushService;

    @Resource
    private Cache<Long, DatasourceClient> datasourceClientCache;

    @Resource
    private Cache<String, SysDatabaseConfig> databaseConfigCache;

    @Resource
    private Cache<Long, SysDatabaseConfigView> databaseConfigViewCache;

    @Resource
    private Cache<Long, List<SysApp>> databaseAppConfigCache;

    @Resource
    private Cache<Long, SysApp> sysAppCache;

    /**
     * analyse
     *
     * @param jdbcTemplate jdbcTemplate
     * @param tableEvent tableEvent
     * @param type type
     * @param rows rows
     */
    public void analyse(JdbcTemplate jdbcTemplate, TableMapEventData tableEvent, DataChangeEnum type,
            List<Serializable[]> rows) {
        String cacheKey = tableEvent.getDatabase() + tableEvent.getTable();
        SysDatabaseConfig config = databaseConfigCache.getIfPresent(cacheKey);
        if (config == null) {
            return;
        }
        List<SysApp> appList = databaseAppConfigCache.getIfPresent(config.getId());
        if (CollectionUtils.isEmpty(appList)) {
            return;
        }
        CompletableUtils.runAsync(() -> {
            SysDatabaseConfigView configView = Objects.requireNonNull(databaseConfigViewCache.get(config.getId(),
                    k -> getDatabaseConfigView(jdbcTemplate, config)));
            dataPushService.push(configView, type, buildDataList(configView.getTableStruct(), rows), appList);
        }).exceptionally(ex -> {
            log.error("数据日志解析异常", ex);
            return null;
        });
    }

    /**
     * analyse
     *
     * @param datasourceId datasourceId
     * @param configIds configIds
     * @param incrementValue incrementValue
     */
    public void analyse(Long datasourceId, Long[] configIds, String incrementValue) {
        JdbcTemplate jdbcTemplate = Objects.requireNonNull(datasourceClientCache.getIfPresent(datasourceId))
                .getJdbcTemplate();
        for (Long configId : configIds) {
            SysDatabaseConfig config = sysDatabaseConfigMapper.selectByPrimaryKey(configId);
            Assert.notNull(config, "配置查询失败");
            checkIncrementValue(config.getIncrementType(), incrementValue);
            List<SysApp> appList = databaseAppConfigCache.getIfPresent(configId);
            if (CollectionUtils.isEmpty(appList)) {
                return;
            }
            SysDatabaseConfigView configView = Objects.requireNonNull(databaseConfigViewCache.get(config.getId(),
                    k -> getDatabaseConfigView(jdbcTemplate, config)));
            List<Map<String, Object>> dataList = buildDataList(jdbcTemplate, config, incrementValue);
            for (Map<String, Object> item : dataList) {
                // 拆成单条，避免数据过大
                dataPushService.push(configView, DataChangeEnum.REPLACE, Collections.singletonList(item), appList);
            }
        }
    }

    /**
     * analyse
     *
     * @param dataLog dataLog
     * @param pushLog pushLog
     */
    public void analyse(SysDataLog dataLog, SysPushLog pushLog) {
        JdbcTemplate jdbcTemplate =
                Objects.requireNonNull(datasourceClientCache.getIfPresent(dataLog.getDatasourceId()))
                        .getJdbcTemplate();
        SysDatabaseConfig config = sysDatabaseConfigMapper.selectByPrimaryKey(dataLog.getDatabaseConfigId());
        Assert.notNull(config, "配置查询失败");
        SysApp sysApp = sysAppCache.getIfPresent(pushLog.getAppId());
        if (sysApp == null) {
            return;
        }
        SysDatabaseConfigView configView = Objects.requireNonNull(databaseConfigViewCache.get(config.getId(),
                k -> getDatabaseConfigView(jdbcTemplate, config)));
        List<Map<String, Object>> dataList = buildDataList(jdbcTemplate, config, dataLog);
        for (Map<String, Object> item : dataList) {
            // 拆成单条，避免数据过大
            dataPushService.push(configView, pushLog, Collections.singletonList(item), sysApp);
        }
    }

    private SysDatabaseConfigView getDatabaseConfigView(JdbcTemplate jdbcTemplate, SysDatabaseConfig config) {
        SysDatabaseConfigView configView = BeanExtUtils.convert(config, SysDatabaseConfigView::new);
        configView.setTableStruct(jdbcTemplate.query(
                StringExtUtils.format(appPropertiesConfig.getSqlTableStruct(),
                        config.getDatabaseName(), config.getTableName()),
                new BeanPropertyRowMapper<>(TableStruct.class)));
        configView.getTableStruct().sort(Comparator.comparingInt(TableStruct::getOrdinalPosition));
        return configView;
    }

    private List<Map<String, Object>> buildDataList(JdbcTemplate jdbcTemplate,
            SysDatabaseConfig config, String incrementValue) {
        String sql = StringExtUtils.format(appPropertiesConfig.getSqlIncrementSearch(),
                config.getDatabaseName(),
                config.getTableName(),
                config.getIncrementKey(),
                incrementValue);
        return jdbcTemplate.queryForList(sql);
    }

    private List<Map<String, Object>> buildDataList(JdbcTemplate jdbcTemplate,
            SysDatabaseConfig config, SysDataLog dataLog) {
        if (config.getPrimaryKey().length == 1) {
            String ids = JSON.parseArray(dataLog.getDataLog(), JSONObject.class).stream()
                    .map(item -> item.getString(config.getPrimaryKey()[0]))
                    .distinct().collect(Collectors.joining("','"));
            String sql = StringExtUtils.format(appPropertiesConfig.getSqlPrimaryKeySearch(),
                    config.getDatabaseName(),
                    config.getTableName(),
                    config.getPrimaryKey(),
                    ids);
            return jdbcTemplate.queryForList(sql);
        }
        List<JSONObject> dataList = JSON.parseArray(dataLog.getDataLog(), JSONObject.class);
        List<Map<String, Object>> result = new ArrayList<>();
        for (JSONObject item : dataList) {
            String where = Arrays.stream(config.getPrimaryKey())
                    .map(key -> key + "='" + item.get(key) + "'").collect(Collectors.joining(" and "));
            String sql = StringExtUtils.format("select * from {}.{} where {}",
                    config.getDatabaseName(),
                    config.getTableName(),
                    where);
            result.addAll(jdbcTemplate.queryForList(sql));
        }
        return result;
    }

    private List<Map<String, Object>> buildDataList(List<TableStruct> tableStructs, List<Serializable[]> rows) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Serializable[] row : rows) {
            Map<String, Object> item = new HashMap<>(row.length);
            for (TableStruct struct : tableStructs) {
                item.put(struct.getColumnName(), formatData(row[struct.getOrdinalPosition() - 1]));
            }
            list.add(item);
        }
        return list;
    }

    private void checkIncrementValue(String incrementType, String incrementValue) {
        if (IncrementTypeEnum.DATE.getCode().equals(incrementType)) {
            LocalDateTime date = LocalDateTime.parse(incrementValue, Consts.TIME_FORMATTER);
            Assert.notNull(date, "时间转换失败");
        }
        if (IncrementTypeEnum.NUMBER.getCode().equals(incrementType)) {
            Long number = NumberUtils.createLong(incrementValue);
            Assert.notNull(number, "数字转换失败");
        }
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
            return ((Timestamp) data).toLocalDateTime().format(Consts.TIME_FORMATTER);
        }
        if (data instanceof java.sql.Date || data instanceof java.sql.Time) {
            return data.toString();
        }
        if (data instanceof Date) {
            return DateFormatUtils.format((Date) data, Consts.TIME_PATTERN);
        }
        return data;
    }

}
