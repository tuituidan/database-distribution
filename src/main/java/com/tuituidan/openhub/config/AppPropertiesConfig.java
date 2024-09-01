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
     * binlogEnabled
     */
    Boolean binlogEnabled;

    /**
     * 表结构sql.
     */
    private String tableStructSql;

    /**
     * jdbcUrl模版.
     */
    private String jdbcUrlTemplate;

}
