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
import com.tuituidan.openhub.mapper.SysAppDatabaseConfigMapper;
import com.tuituidan.openhub.mapper.SysAppMapper;
import com.tuituidan.openhub.mapper.SysDatabaseConfigMapper;
import com.tuituidan.tresdin.util.BeanExtUtils;
import com.tuituidan.tresdin.util.StringExtUtils;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
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

    @Override
    public void run(ApplicationArguments args) throws Exception {
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
        dataPushService.analyse(databaseConfigViewCache.get(config.getId(),
                k -> getDatabaseConfigView(jdbcTemplate, config, appList)), type, rows);
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

}
