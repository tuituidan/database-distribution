package com.tuituidan.openhub.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.tuituidan.openhub.bean.dto.SysAppParam;
import com.tuituidan.openhub.bean.entity.SysApp;
import com.tuituidan.openhub.bean.entity.SysAppDatabaseConfig;
import com.tuituidan.openhub.mapper.SysAppDatabaseConfigMapper;
import com.tuituidan.openhub.mapper.SysAppMapper;
import com.tuituidan.tresdin.util.BeanExtUtils;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * SysAppService.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/8/31
 */
@Service
public class SysAppService implements ApplicationRunner {

    @Resource
    private SysAppMapper sysAppMapper;

    @Resource
    private SysAppDatabaseConfigMapper sysAppDatabaseConfigMapper;

    @Resource
    private Cache<Long, SysApp> sysAppCache;

    @Resource
    private Cache<Long, List<SysApp>> databaseAppConfigCache;

    @Override
    public void run(ApplicationArguments args) {
        List<SysApp> sysApps = sysAppMapper.selectAll();
        for (SysApp sysApp : sysApps) {
            sysAppCache.put(sysApp.getId(), sysApp);
        }
    }

    /**
     * selectAll
     *
     * @return List
     */
    public List<SysApp> selectAll() {
        return sysAppMapper.selectAll();
    }

    /**
     * save
     *
     * @param id id
     * @param param param
     */
    public void save(Long id, SysAppParam param) {
        checkUnique(id, param);
        SysApp saveItem = BeanExtUtils.convert(param, SysApp::new);
        if (id == null) {
            sysAppMapper.insertSelective(saveItem);
            sysAppCache.put(saveItem.getId(), saveItem);
            return;
        }
        saveItem.setId(id);
        sysAppMapper.updateByPrimaryKeySelective(saveItem);
        sysAppCache.put(id, saveItem);
        Set<Long> configIds = sysAppDatabaseConfigMapper.select(new SysAppDatabaseConfig().setAppId(id))
                .stream().map(SysAppDatabaseConfig::getDatabaseConfigId).collect(Collectors.toSet());
        databaseAppConfigCache.invalidateAll(configIds);
    }

    private void checkUnique(Long id, SysAppParam param) {
        SysApp exist = sysAppMapper.selectOne(new SysApp().setAppKey(param.getAppKey()));
        Assert.isTrue(Objects.isNull(exist) || Objects.equals(id, exist.getId()),
                "已存在相同应用标识的应用");
    }

    /**
     * delete
     *
     * @param id id
     */
    public void delete(Long id) {
        sysAppMapper.deleteByPrimaryKey(id);
        sysAppDatabaseConfigMapper.delete(new SysAppDatabaseConfig().setAppId(id));
        sysAppCache.invalidate(id);
        Set<Long> configIds = sysAppDatabaseConfigMapper.select(new SysAppDatabaseConfig().setAppId(id))
                .stream().map(SysAppDatabaseConfig::getDatabaseConfigId).collect(Collectors.toSet());
        databaseAppConfigCache.invalidateAll(configIds);
    }

}
