package com.tuituidan.openhub.service;

import com.tuituidan.openhub.bean.entity.SysAppDatabaseConfig;
import com.tuituidan.openhub.bean.entity.SysDataSource;
import com.tuituidan.openhub.bean.entity.SysDatabaseConfig;
import com.tuituidan.openhub.bean.vo.TreeView;
import com.tuituidan.openhub.mapper.SysAppDatabaseConfigMapper;
import com.tuituidan.openhub.mapper.SysDataSourceMapper;
import com.tuituidan.openhub.mapper.SysDatabaseConfigMapper;
import com.tuituidan.tresdin.consts.Separator;
import com.tuituidan.tresdin.util.ListExtUtils;
import com.tuituidan.tresdin.util.StringExtUtils;
import com.tuituidan.tresdin.util.tree.TreeUtils;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

/**
 * SysAppService.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/8/31
 */
@Service
public class AppDatabaseConfigService {

    @Resource
    private SysDatabaseConfigMapper sysDatabaseConfigMapper;

    @Resource
    private SysDataSourceMapper sysDataSourceMapper;

    @Resource
    private SysAppDatabaseConfigMapper sysAppDatabaseConfigMapper;

    @Resource
    private CacheService cacheService;

    /**
     * selectIds
     *
     * @param appId appId
     * @return List
     */
    public List<Long> selectIds(Long appId) {
        return sysAppDatabaseConfigMapper.select(new SysAppDatabaseConfig().setAppId(appId))
                .stream().map(SysAppDatabaseConfig::getDatabaseConfigId).collect(Collectors.toList());
    }

    /**
     * selectAll
     *
     * @return List
     */
    public List<TreeView> selectTree() {
        List<SysDatabaseConfig> databaseConfigs = sysDatabaseConfigMapper.selectAll();
        if (CollectionUtils.isEmpty(databaseConfigs)) {
            return Collections.emptyList();
        }

        String ids = StringUtils.join(databaseConfigs.stream().map(SysDatabaseConfig::getDatasourceId)
                .collect(Collectors.toSet()), Separator.COMMA);
        List<SysDataSource> dataSources = sysDataSourceMapper.selectByIds(ids);

        List<TreeView> resultList = dataSources.stream()
                .map(item -> new TreeView().setType("datasource")
                        .setId(item.getId().toString())
                        .setName(StringExtUtils.format("{}({}:{})",
                                item.getName(), item.getHost(), item.getPort())))
                .collect(Collectors.toList());
        Map<String, TreeView> databaseNodeMap = new HashMap<>(databaseConfigs.size());
        for (SysDatabaseConfig config : databaseConfigs) {
            String dbNodeKey = config.getDatasourceId() + config.getDatabaseName();
            TreeView dbNode = databaseNodeMap.computeIfAbsent(dbNodeKey,
                    k -> {
                        TreeView parentNode = new TreeView()
                                .setId(StringExtUtils.getUuid())
                                .setType("database")
                                .setName(config.getDatabaseName())
                                .setPid(config.getDatasourceId().toString());
                        resultList.add(parentNode);
                        return parentNode;
                    });
            resultList.add(new TreeView()
                    .setId(config.getId().toString())
                    .setType("table")
                    .setPid(dbNode.getId())
                    .setName(StringExtUtils.format("{}({})",
                            config.getTableComment(), config.getTableName())));
        }
        return TreeUtils.buildTree(resultList);
    }

    /**
     * saveAppDatabaseConfig
     *
     * @param appId appId
     * @param configIds configIds
     */
    public void saveAppDatabaseConfig(Long appId, Long[] configIds) {
        Set<Long> existIds = sysAppDatabaseConfigMapper.select(new SysAppDatabaseConfig().setAppId(appId))
                .stream().map(SysAppDatabaseConfig::getDatabaseConfigId).collect(Collectors.toSet());
        Pair<Set<Long>, Set<Long>> pair = ListExtUtils.splitSaveIds(configIds, existIds);
        if (CollectionUtils.isNotEmpty(pair.getLeft())) {
            sysAppDatabaseConfigMapper.deleteByAppIdAndConfigIds(appId, pair.getLeft());
        }
        if (CollectionUtils.isNotEmpty(pair.getRight())) {
            Map<Long, Long> datasourceIdMap = sysDatabaseConfigMapper.selectByIds(StringUtils.join(pair.getRight(),
                            ","))
                    .stream().collect(Collectors.toMap(SysDatabaseConfig::getId, SysDatabaseConfig::getDatasourceId));
            sysAppDatabaseConfigMapper.insertList(pair.getRight().stream()
                    .map(configId -> new SysAppDatabaseConfig().setAppId(appId)
                            .setDatasourceId(datasourceIdMap.get(configId))
                            .setDatabaseConfigId(configId))
                    .collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(pair.getLeft()) || CollectionUtils.isNotEmpty(pair.getRight())) {
            HashSet<Long> ids = new HashSet<>(pair.getLeft());
            ids.addAll(pair.getRight());
            cacheService.refreshDataConfigCache(ids.toArray(new Long[0]));
        }
    }

}
