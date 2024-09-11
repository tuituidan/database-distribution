package com.tuituidan.openhub.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.tuituidan.openhub.bean.dto.SysDatabaseConfigParam;
import com.tuituidan.openhub.bean.entity.SysApp;
import com.tuituidan.openhub.bean.entity.SysAppDatabaseConfig;
import com.tuituidan.openhub.bean.entity.SysDatabaseConfig;
import com.tuituidan.openhub.bean.vo.SysDatabaseConfigView;
import com.tuituidan.openhub.mapper.SysAppDatabaseConfigMapper;
import com.tuituidan.openhub.mapper.SysAppMapper;
import com.tuituidan.openhub.mapper.SysDatabaseConfigMapper;
import com.tuituidan.tresdin.util.BeanExtUtils;
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
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
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
    private SysDatabaseConfigMapper sysDatabaseConfigMapper;

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

}
