package com.tuituidan.openhub.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.shyiko.mysql.binlog.event.TableMapEventData;
import com.tuituidan.openhub.bean.entity.SysApp;
import com.tuituidan.openhub.bean.entity.SysAppDataRule;
import com.tuituidan.openhub.bean.entity.SysAppDatabaseConfig;
import com.tuituidan.openhub.bean.entity.SysDataSource;
import com.tuituidan.openhub.bean.entity.SysDatabaseConfig;
import com.tuituidan.openhub.bean.vo.DataConfigView;
import com.tuituidan.openhub.bean.vo.SysAppView;
import com.tuituidan.openhub.bean.vo.TableStruct;
import com.tuituidan.openhub.config.AppPropertiesConfig;
import com.tuituidan.openhub.consts.Consts;
import com.tuituidan.openhub.consts.enums.DataChangeEnum;
import com.tuituidan.openhub.mapper.SysAppDataRuleMapper;
import com.tuituidan.openhub.mapper.SysAppDatabaseConfigMapper;
import com.tuituidan.openhub.mapper.SysAppMapper;
import com.tuituidan.openhub.mapper.SysDataSourceMapper;
import com.tuituidan.openhub.mapper.SysDatabaseConfigMapper;
import com.tuituidan.tresdin.util.BeanExtUtils;
import com.tuituidan.tresdin.util.StringExtUtils;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.stereotype.Service;

/**
 * CacheService.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/16
 */
@Service
public class CacheService implements ApplicationRunner {

    @Resource
    private Cache<Long, DatasourceClient> datasourceClientCache;

    @Resource
    private Cache<String, String> appTokenCache;

    @Resource
    private Cache<Long, SysApp> sysAppCache;

    @Resource
    private Cache<Long, SysDataSource> sysDataSourceCache;

    @Resource
    private Cache<String, DataConfigView> dataConfigCache;

    @Resource
    private SysDatabaseConfigMapper sysDatabaseConfigMapper;

    @Resource
    private SysAppDatabaseConfigMapper sysAppDatabaseConfigMapper;

    @Resource
    private SysAppMapper sysAppMapper;

    @Resource
    private SysDataSourceMapper sysDataSourceMapper;

    @Resource
    private SysAppDataRuleMapper sysAppDataRuleMapper;

    @Resource
    private AppPropertiesConfig appPropertiesConfig;

    @Resource
    private DataAnalyseService dataAnalyseService;

    /**
     * caches
     *
     * @return Map
     */
    public Map<String, Object> caches() {
        Map<String, Object> caches = new HashMap<>();
        caches.put("sysAppCache", sysAppCache.asMap().keySet());
        caches.put("sysDataSourceCache", sysDataSourceCache.asMap().keySet());
        caches.put("appTokenCache", appTokenCache.asMap().keySet());
        caches.put("datasourceClientCache", datasourceClientCache.asMap().keySet());
        caches.put("dataConfigCache", dataConfigCache.asMap().keySet());
        caches.put("currentTime", LocalDateTime.now().format(Consts.TIME_FORMATTER));
        return caches;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<SysDataSource> dataSourceList = sysDataSourceMapper.selectAll();
        for (SysDataSource dataSource : dataSourceList) {
            datasourceClientCache.put(dataSource.getId(), createClient(dataSource));
            sysDataSourceCache.put(dataSource.getId(), dataSource);
        }

        List<SysDatabaseConfig> databaseConfigList = sysDatabaseConfigMapper.selectAll();
        Map<Long, List<SysAppView>> configAppMap = buildConfigAppList();
        for (SysDatabaseConfig config : databaseConfigList) {
            dataConfigCache.put(config.getDatabaseName() + config.getTableName(),
                    buildDataConfigView(config).setAppList(configAppMap.get(config.getId())));
        }
    }

    private DatasourceClient createClient(SysDataSource dataSource) {
        return new DatasourceClient(dataSource, appPropertiesConfig) {
            @Override
            public void handler(TableMapEventData tableEvent, DataChangeEnum type,
                    List<Serializable[]> rows) {
                dataAnalyseService.analyse(tableEvent, type, rows);
            }
        };
    }

    /**
     * 刷新数据源缓存
     *
     * @param dataSourceId dataSourceId
     * @param dataSource dataSource
     */
    public void refreshDataSourceCache(Long dataSourceId, SysDataSource dataSource) {
        sysDataSourceCache.invalidate(dataSourceId);
        datasourceClientCache.invalidate(dataSourceId);
        if (dataSource == null) {
            return;
        }
        sysDataSourceCache.put(dataSourceId, dataSource);
        datasourceClientCache.put(dataSourceId, createClient(dataSource));
    }

    /**
     * 根据应用ID刷新数据配置缓存
     *
     * @param appId appId
     */
    public void refreshCacheByAppId(Long appId) {
        Long[] ids = sysAppDatabaseConfigMapper.select(new SysAppDatabaseConfig().setAppId(appId))
                .stream().map(SysAppDatabaseConfig::getDatabaseConfigId).distinct().toArray(Long[]::new);
        refreshDataConfigCache(ids);
    }

    /**
     * 刷新数据配置缓存
     *
     * @param configIds configIds
     */
    public void refreshDataConfigCache(Long... configIds) {
        List<SysDatabaseConfig> configList = sysDatabaseConfigMapper.selectByIds(StringUtils.join(configIds,
                ","));
        Map<Long, List<SysAppView>> configAppMap = buildConfigAppList();
        for (SysDatabaseConfig config : configList) {
            dataConfigCache.put(config.getDatabaseName() + config.getTableName(),
                    buildDataConfigView(config).setAppList(configAppMap.get(config.getId())));
        }
    }

    private DataConfigView buildDataConfigView(SysDatabaseConfig config) {
        DataConfigView configView = BeanExtUtils.convert(config, DataConfigView::new);
        DatasourceClient datasourceClient =
                Objects.requireNonNull(datasourceClientCache.getIfPresent(config.getDatasourceId()));
        configView.setJdbcTemplate(datasourceClient.getJdbcTemplate());
        configView.setTimeZone(datasourceClient.getDataSource().getTimeZone());
        configView.setTableStruct(datasourceClient.getJdbcTemplate().query(
                StringExtUtils.format(appPropertiesConfig.getSqlTableStruct(),
                        config.getDatabaseName(), config.getTableName()),
                new BeanPropertyRowMapper<>(TableStruct.class)));
        configView.getTableStruct().sort(Comparator.comparingInt(TableStruct::getOrdinalPosition));
        return configView;
    }

    private Map<Long, List<SysAppView>> buildConfigAppList() {
        List<SysAppDatabaseConfig> appConfigList = sysAppDatabaseConfigMapper.selectAll();
        Map<Long, SysApp> appMap = sysAppMapper.selectAll().stream().collect(Collectors.toMap(SysApp::getId,
                Function.identity()));
        Map<String, List<SysAppDataRule>> appDataRuleMap =
                sysAppDataRuleMapper.selectAll().stream().collect(Collectors.groupingBy(item ->
                        item.getDatabaseConfigId() + "" + item.getAppId()));
        return appConfigList.stream()
                .collect(Collectors.groupingBy(SysAppDatabaseConfig::getDatabaseConfigId,
                        Collectors.mapping(item -> buildConfigAppList(item, appMap, appDataRuleMap),
                                Collectors.toList())));
    }

    private SysAppView buildConfigAppList(SysAppDatabaseConfig item, Map<Long, SysApp> appMap,
            Map<String, List<SysAppDataRule>> appDataRuleMap) {
        return BeanExtUtils.convert(appMap.get(item.getAppId()), SysAppView::new)
                .setDataRules(appDataRuleMap.get(item.getDatabaseConfigId() + "" + item.getAppId()));
    }

}
