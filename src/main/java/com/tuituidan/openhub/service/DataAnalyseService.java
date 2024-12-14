package com.tuituidan.openhub.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.tuituidan.openhub.bean.entity.SysAppDataRule;
import com.tuituidan.openhub.bean.entity.SysDataLog;
import com.tuituidan.openhub.bean.entity.SysDatabaseConfig;
import com.tuituidan.openhub.bean.entity.SysPushLog;
import com.tuituidan.openhub.bean.vo.DataConfigView;
import com.tuituidan.openhub.bean.vo.SysAppView;
import com.tuituidan.openhub.bean.vo.TableStruct;
import com.tuituidan.openhub.config.AppPropertiesConfig;
import com.tuituidan.openhub.consts.Consts;
import com.tuituidan.openhub.consts.enums.DataChangeEnum;
import com.tuituidan.openhub.consts.enums.FilterTypeEnum;
import com.tuituidan.openhub.consts.enums.IncrementTypeEnum;
import com.tuituidan.openhub.mapper.SysDatabaseConfigMapper;
import com.tuituidan.tresdin.util.ExpParserUtils;
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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
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
    private Cache<String, DataConfigView> dataConfigCache;

    /**
     * 数据库监听数据解析
     *
     * @param tableEvent tableEvent
     * @param type type
     * @param rows rows
     */
    public void analyse(TableMapEventData tableEvent, DataChangeEnum type,
            List<Serializable[]> rows) {
        DataConfigView configView = dataConfigCache.getIfPresent(tableEvent.getDatabase()
                + tableEvent.getTable());
        if (configView == null || CollectionUtils.isEmpty(configView.getAppList())) {
            return;
        }
        CompletableUtils.runAsync(() -> {
            List<JSONObject> dataList = buildDataList(configView, rows);
            for (JSONObject data : dataList) {
                List<SysAppView> appList = filterAppByData(configView.getJdbcTemplate(), configView.getAppList(), data);
                if (CollectionUtils.isEmpty(appList)) {
                    continue;
                }
                dataPushService.push(configView, type, Collections.singletonList(data), appList);
            }

        }, "data-analyse").exceptionally(ex -> {
            log.error("数据日志解析异常", ex);
            return null;
        });
    }

    /**
     * 手动增量推送
     *
     * @param configIds configIds
     * @param incrementValue incrementValue
     */
    public void analyse(Long[] configIds, String incrementValue) {
        for (Long configId : configIds) {
            SysDatabaseConfig config = sysDatabaseConfigMapper.selectByPrimaryKey(configId);
            Assert.notNull(config, "配置查询失败");
            checkIncrementValue(config.getIncrementType(), incrementValue);
            DataConfigView configView = dataConfigCache.getIfPresent(config.getDatabaseName()
                    + config.getTableName());
            if (configView == null || CollectionUtils.isEmpty(configView.getAppList())) {
                return;
            }
            List<JSONObject> dataList = buildDataList(configView, incrementValue);
            for (JSONObject data : dataList) {
                List<SysAppView> appList = filterAppByData(configView.getJdbcTemplate(), configView.getAppList(), data);
                if (CollectionUtils.isEmpty(appList)) {
                    continue;
                }
                dataPushService.push(configView, DataChangeEnum.REPLACE, Collections.singletonList(data), appList);
            }
        }
    }

    /**
     * 手动推送失败数据
     *
     * @param dataLog dataLog
     * @param pushLog pushLog
     */
    public void analyse(SysDataLog dataLog, SysPushLog pushLog) {
        SysDatabaseConfig config = sysDatabaseConfigMapper.selectByPrimaryKey(dataLog.getDatabaseConfigId());
        Assert.notNull(config, "配置查询失败");
        DataConfigView configView = dataConfigCache.getIfPresent(config.getDatabaseName()
                + config.getTableName());
        if (configView == null || CollectionUtils.isEmpty(configView.getAppList())) {
            return;
        }
        SysAppView sysApp = configView.getAppList().stream().filter(item -> Objects.equals(pushLog.getAppId(),
                        item.getId()))
                .findFirst().orElse(null);
        if (sysApp == null) {
            return;
        }
        List<JSONObject> dataList = buildDataList(configView, dataLog);
        for (JSONObject data : dataList) {
            if (CollectionUtils.isEmpty(sysApp.getDataRules())
                    || filterByAppRules(configView.getJdbcTemplate(), sysApp.getDataRules(), data)) {
                dataPushService.push(configView, pushLog, Collections.singletonList(data), sysApp);
            }
        }
    }

    private List<SysAppView> filterAppByData(JdbcTemplate jdbcTemplate,
            List<SysAppView> appList, JSONObject data) {
        List<SysAppView> resultList = new ArrayList<>();
        for (SysAppView appView : appList) {
            if (CollectionUtils.isEmpty(appView.getDataRules())
                    || filterByAppRules(jdbcTemplate, appView.getDataRules(), data)) {
                resultList.add(appView);
            }
        }
        return resultList;
    }

    private boolean filterByAppRules(JdbcTemplate jdbcTemplate,
            List<SysAppDataRule> rules, JSONObject data) {
        boolean filterResult;
        for (SysAppDataRule rule : rules) {
            if (FilterTypeEnum.EXP.getCode().equals(rule.getRuleType())) {
                filterResult = ExpParserUtils.evaluate(rule.getRuleExp(), data);
                if (!filterResult) {
                    return false;
                }
            }
            if (FilterTypeEnum.SQL.getCode().equals(rule.getRuleType())) {
                String sql = ExpParserUtils.template(rule.getRuleExp(), data);
                filterResult = CollectionUtils.isNotEmpty(jdbcTemplate.queryForList(sql));
                if (!filterResult) {
                    return false;
                }
            }
        }
        return true;
    }

    private List<JSONObject> buildDataList(DataConfigView configView, String incrementValue) {
        Assert.hasText(configView.getIncrementKey(), "未配置增量字段，无法增量同步");
        String sql = StringExtUtils.format(appPropertiesConfig.getSqlDynamicSearch(),
                configView.getDatabaseName(),
                configView.getTableName(),
                StringExtUtils.format("{} > '{}'", configView.getIncrementKey(), incrementValue));
        return mapToJsonList(configView.getJdbcTemplate().queryForList(sql));
    }

    private List<JSONObject> buildDataList(DataConfigView configView, SysDataLog dataLog) {
        Assert.notEmpty(configView.getPrimaryKey(), "未配置主键，无法查询最新数据进行同步");
        if (configView.getPrimaryKey().length == 1) {
            String ids = JSON.parseArray(dataLog.getDataLog(), JSONObject.class).stream()
                    .map(item -> item.getString(configView.getPrimaryKey()[0]))
                    .distinct().collect(Collectors.joining("','"));
            String sql = StringExtUtils.format(appPropertiesConfig.getSqlDynamicSearch(),
                    configView.getDatabaseName(),
                    configView.getTableName(),
                    StringExtUtils.format("{} in ('{}')", configView.getPrimaryKey()[0], ids));
            return mapToJsonList(configView.getJdbcTemplate().queryForList(sql));
        }
        List<JSONObject> dataList = JSON.parseArray(dataLog.getDataLog(), JSONObject.class);
        List<Map<String, Object>> result = new ArrayList<>();
        for (JSONObject item : dataList) {
            String sql = StringExtUtils.format(appPropertiesConfig.getSqlDynamicSearch(),
                    configView.getDatabaseName(),
                    configView.getTableName(),
                    Arrays.stream(configView.getPrimaryKey())
                            .map(key -> key + "='" + item.get(key) + "'")
                            .collect(Collectors.joining(" and ")));
            result.addAll(configView.getJdbcTemplate().queryForList(sql));
        }
        return mapToJsonList(result);
    }

    private List<JSONObject> buildDataList(DataConfigView configView, List<Serializable[]> rows) {
        List<JSONObject> list = new ArrayList<>();
        for (Serializable[] row : rows) {
            JSONObject item = new JSONObject();
            for (TableStruct struct : configView.getTableStruct()) {
                item.put(struct.getColumnName(), formatData(configView.getTimeZone(),
                        row[struct.getOrdinalPosition() - 1]));
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

    private List<JSONObject> mapToJsonList(List<Map<String, Object>> source) {
        return JSON.parseArray(JSON.toJSONString(source), JSONObject.class);
    }

    private Object formatData(String timeZone, Object data) {
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
        if (data instanceof java.sql.Date) {
            return data.toString();
        }
        if (data instanceof java.sql.Time) {
            if (StringUtils.isBlank(timeZone)) {
                return data.toString();
            }
            return DateFormatUtils.format((Date) data,
                    DateFormatUtils.ISO_8601_EXTENDED_TIME_FORMAT.getPattern(),
                    TimeZone.getTimeZone(timeZone));
        }
        if (data instanceof Date) {
            if (StringUtils.isBlank(timeZone)) {
                return DateFormatUtils.format((Date) data, Consts.TIME_PATTERN);
            }
            return DateFormatUtils.format((Date) data, Consts.TIME_PATTERN, TimeZone.getTimeZone(timeZone));
        }
        return data;
    }

}
