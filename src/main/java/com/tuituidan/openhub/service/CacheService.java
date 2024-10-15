package com.tuituidan.openhub.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.tuituidan.openhub.bean.entity.SysApp;
import com.tuituidan.openhub.bean.entity.SysDataSource;
import com.tuituidan.openhub.bean.entity.SysDatabaseConfig;
import com.tuituidan.openhub.bean.vo.SysAppView;
import com.tuituidan.openhub.bean.vo.SysDatabaseConfigView;
import com.tuituidan.openhub.consts.Consts;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * CacheService.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/16
 */
@Service
public class CacheService {

    @Resource
    private Cache<Long, DatasourceClient> datasourceClientCache;

    @Resource
    private Cache<String, SysDatabaseConfig> databaseConfigCache;

    @Resource
    private Cache<Long, List<SysAppView>> databaseAppConfigCache;

    @Resource
    private Cache<Long, SysDatabaseConfigView> databaseConfigViewCache;

    @Resource
    private Cache<String, String> appTokenCache;

    @Resource
    private Cache<Long, SysApp> sysAppCache;

    @Resource
    private Cache<Long, SysDataSource> sysDataSourceCache;

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
        caches.put("databaseConfigViewCache", databaseConfigViewCache.asMap().keySet());
        caches.put("databaseAppConfigCache", databaseAppConfigCache.asMap().keySet());
        caches.put("databaseConfigCache", databaseConfigCache.asMap().keySet());
        caches.put("datasourceClientCache", datasourceClientCache.asMap().keySet());
        caches.put("currentTime", LocalDateTime.now().format(Consts.TIME_FORMATTER));
        return caches;
    }

}
