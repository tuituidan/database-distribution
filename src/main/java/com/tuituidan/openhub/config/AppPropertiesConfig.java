package com.tuituidan.openhub.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AppPropertiesConfig.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/1
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppPropertiesConfig {

    /**
     * 获取数据库sql.
     */
    private String sqlDatabase;

    /**
     * 获取数据表sql.
     */
    private String sqlDatabaseTable;

    /**
     * 获取数据表字段sql.
     */
    private String sqlDatabaseTableColumn;

    /**
     * 表结构sql.
     */
    private String sqlTableStruct;

    /**
     * 动态查询
     */
    private String sqlDynamicSearch;

    /**
     * jdbcUrl模版.
     */
    private String jdbcUrlTemplate;

}
