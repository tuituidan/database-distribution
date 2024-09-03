package com.tuituidan.openhub.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.tuituidan.openhub.bean.entity.SysDatabaseConfig;
import com.tuituidan.openhub.bean.vo.SysDatabaseConfigView;
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
     * databaseConfigViewCache
     *
     * @return Cache
     */
    @Bean
    public Cache<String, SysDatabaseConfigView> databaseConfigViewCache() {
        return Caffeine.newBuilder().build();
    }

    /**
     * databaseConfigCache
     *
     * @return Cache
     */
    @Bean
    public Cache<String, SysDatabaseConfig> databaseConfigCache() {
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

}
