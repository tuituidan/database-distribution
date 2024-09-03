package com.tuituidan.openhub.bean.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tuituidan.tresdin.mybatis.bean.IEntity;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * SysDataSource.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/8/31
 */
@Getter
@Setter
@Accessors(chain = true)
@Table(name = "sys_database_config", schema = "database_distribution")
public class SysDatabaseConfig implements IEntity<SysDatabaseConfig, Long> {

    @Id
    private Long id;

    @Column(name = "datasource_id")
    private Long datasourceId;

    @Column(name = "database_name")
    private String databaseName;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "primary_key")
    private String primaryKey;

    @Column(name = "increment_key")
    private String incrementKey;

    @Column(name = "increment_type")
    private String incrementType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "update_time")
    private LocalDateTime updateTime;

}