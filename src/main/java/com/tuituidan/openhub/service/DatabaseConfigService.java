package com.tuituidan.openhub.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.tuituidan.openhub.bean.dto.SysDatabaseConfigParam;
import com.tuituidan.openhub.bean.entity.SysApp;
import com.tuituidan.openhub.bean.entity.SysAppDatabaseConfig;
import com.tuituidan.openhub.bean.entity.SysDatabaseConfig;
import com.tuituidan.openhub.bean.vo.SysDatabaseConfigView;
import com.tuituidan.openhub.bean.vo.TableStruct;
import com.tuituidan.openhub.config.AppPropertiesConfig;
import com.tuituidan.openhub.consts.Consts;
import com.tuituidan.openhub.consts.enums.DataChangeEnum;
import com.tuituidan.openhub.consts.enums.IncrementTypeEnum;
import com.tuituidan.openhub.mapper.SysAppDatabaseConfigMapper;
import com.tuituidan.openhub.mapper.SysAppMapper;
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
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * DatabaseConfigService.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/3
 */
@Service
@Slf4j
public class DatabaseConfigService implements ApplicationRunner {

    @Resource
    private SysAppDatabaseConfigMapper sysAppDatabaseConfigMapper;

    @Resource
    private SysAppMapper sysAppMapper;

    @Resource
    private Cache<String, SysDatabaseConfig> databaseConfigCache;

    @Resource
    private Cache<Long, SysDatabaseConfigView> databaseConfigViewCache;

    @Resource
    private Cache<Long, List<SysApp>> databaseAppConfigCache;

    @Resource
    private AppPropertiesConfig appPropertiesConfig;

    @Resource
    private SysDatabaseConfigMapper sysDatabaseConfigMapper;

    @Resource
    private DataPushService dataPushService;

    @Resource
    private Cache<Long, DatasourceClient> datasourceClientCache;

    @Override
    public void run(ApplicationArguments args) {
        List<SysDatabaseConfig> configs = sysDatabaseConfigMapper.selectAll();
        for (SysDatabaseConfig config : configs) {
            databaseConfigCache.put(config.getDatabaseName() + config.getTableName(), config);
        }

        Map<Long, SysApp> appMap = sysAppMapper.selectAll().stream().collect(Collectors.toMap(SysApp::getId,
                Function.identity()));
        List<SysAppDatabaseConfig> configList = sysAppDatabaseConfigMapper.selectAll();
        if (CollectionUtils.isEmpty(configList)) {
            return;
        }
        Map<Long, Set<Long>> configMap =
                configList.stream().collect(Collectors.groupingBy(SysAppDatabaseConfig::getDatabaseConfigId,
                        Collectors.mapping(SysAppDatabaseConfig::getAppId, Collectors.toSet())));
        for (Entry<Long, Set<Long>> entry : configMap.entrySet()) {
            databaseAppConfigCache.put(entry.getKey(), entry.getValue().stream()
                    .map(appMap::get).collect(Collectors.toList()));
        }
    }

    /**
     * select
     *
     * @param param param
     * @return List
     */
    public List<SysDatabaseConfig> select(SysDatabaseConfigParam param) {
        return sysDatabaseConfigMapper.select(BeanExtUtils.convert(param, SysDatabaseConfig::new));
    }

    /**
     * save
     *
     * @param id id
     * @param param param
     */
    public void save(Long id, SysDatabaseConfigParam param) {
        checkUnique(id, param);
        SysDatabaseConfig saveItem = BeanExtUtils.convert(param, SysDatabaseConfig::new);
        if (id == null) {
            sysDatabaseConfigMapper.insertSelective(saveItem);
        } else {
            saveItem.setId(id);
            sysDatabaseConfigMapper.updateByPrimaryKeySelective(saveItem);
        }
        String cacheKey = param.getDatabaseName() + param.getTableName();
        databaseConfigCache.put(cacheKey, saveItem);
        databaseConfigViewCache.invalidate(saveItem.getId());
    }

    private void checkUnique(Long id, SysDatabaseConfigParam param) {
        SysDatabaseConfig search = new SysDatabaseConfig();
        BeanExtUtils.copyProperties(param, search, "datasourceId", "databaseName", "tableName");
        SysDatabaseConfig exist = sysDatabaseConfigMapper.selectOne(search);
        Assert.isTrue(Objects.isNull(exist) || Objects.equals(id, exist.getId()),
                "已存在相同配置（数据源、数据库名、表名一致）");
    }

    /**
     * delete
     *
     * @param id id
     */
    public void delete(Long id) {
        SysDatabaseConfig config = sysDatabaseConfigMapper.selectByPrimaryKey(id);
        Assert.notNull(config, "配置不存在");
        sysDatabaseConfigMapper.deleteByPrimaryKey(id);
        String cacheKey = config.getDatabaseName() + config.getTableName();
        databaseConfigCache.invalidate(cacheKey);
        databaseConfigViewCache.invalidate(id);
    }

    /**
     * analyse
     *
     * @param jdbcTemplate jdbcTemplate
     * @param tableEvent tableEvent
     * @param type type
     * @param rows rows
     */
    public void analyse(JdbcTemplate jdbcTemplate, TableMapEventData tableEvent, String type,
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
                    k -> getDatabaseConfigView(jdbcTemplate, config, appList)));
            dataPushService.push(configView, type, buildDataList(configView.getTableStruct(), rows));
        }).exceptionally(ex -> {
            log.error("数据日志解析异常", ex);
            return null;
        });
    }

    /**
     * handler
     *
     * @param configId configId
     * @param incrementValue incrementValue
     */
    public void handler(Long configId, String incrementValue) {
        SysDatabaseConfig config = sysDatabaseConfigMapper.selectByPrimaryKey(configId);
        Assert.notNull(config, "配置查询失败");
        checkIncrementValue(config.getIncrementType(), incrementValue);
        List<SysApp> appList = databaseAppConfigCache.getIfPresent(configId);
        if (CollectionUtils.isEmpty(appList)) {
            return;
        }
        JdbcTemplate jdbcTemplate = Objects.requireNonNull(datasourceClientCache.getIfPresent(config.getDatasourceId()))
                .getJdbcTemplate();
        SysDatabaseConfigView configView = Objects.requireNonNull(databaseConfigViewCache.get(config.getId(),
                k -> getDatabaseConfigView(jdbcTemplate, config, appList)));
        List<Map<String, Object>> dataList = buildDataList(jdbcTemplate, config, incrementValue);
        for (Map<String, Object> item : dataList) {
            // 拆成单条，避免数据过大
            dataPushService.push(configView, DataChangeEnum.REPLACE.getCode(), Collections.singletonList(item));
        }
    }

    private SysDatabaseConfigView getDatabaseConfigView(JdbcTemplate jdbcTemplate,
            SysDatabaseConfig config, List<SysApp> appList) {
        SysDatabaseConfigView configView = BeanExtUtils.convert(config, SysDatabaseConfigView::new);
        configView.setTableStruct(jdbcTemplate.query(
                StringExtUtils.format(appPropertiesConfig.getSqlTableStruct(),
                        config.getDatabaseName(), config.getTableName()),
                new BeanPropertyRowMapper<>(TableStruct.class)));
        configView.getTableStruct().sort(Comparator.comparingInt(TableStruct::getOrdinalPosition));
        configView.setAppList(appList);
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
