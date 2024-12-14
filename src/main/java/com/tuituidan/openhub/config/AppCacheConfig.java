package com.tuituidan.openhub.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.tuituidan.openhub.bean.entity.SysApp;
import com.tuituidan.openhub.bean.entity.SysDataSource;
import com.tuituidan.openhub.bean.vo.DataConfigView;
import com.tuituidan.openhub.service.DatasourceClient;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AppCacheConfig.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/3
 */
@Configuration
public class AppCacheConfig {

    /**
     * datasourceClientCache
     *
     * @return Cache
     */
    @Bean
    public Cache<Long, DatasourceClient> datasourceClientCache() {
        return Caffeine.newBuilder().build();
    }

    /**
     * databaseConfigCache
     *
     * @return Cache
     */
    @Bean
    public Cache<String, DataConfigView> dataConfigCache() {
        return Caffeine.newBuilder().build();
    }

    /**
     * appTokenCache
     *
     * @return Cache
     */
    @Bean
    public Cache<String, String> appTokenCache() {
        return Caffeine.newBuilder().expireAfterWrite(20, TimeUnit.MINUTES).build();
    }

    /**
     * sysAppCache
     *
     * @return Cache
     */
    @Bean
    public Cache<Long, SysApp> sysAppCache() {
        return Caffeine.newBuilder().build();
    }

    /**
     * sysDataSourceCache
     *
     * @return Cache
     */
    @Bean
    public Cache<Long, SysDataSource> sysDataSourceCache() {
        return Caffeine.newBuilder().build();
    }

}
