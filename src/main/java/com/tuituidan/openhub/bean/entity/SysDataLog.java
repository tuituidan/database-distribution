package com.tuituidan.openhub.bean.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.tuituidan.openhub.consts.Consts;
import com.tuituidan.tresdin.mybatis.bean.IEntity;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * SysApp.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/8/31
 */
@Getter
@Setter
@Accessors(chain = true)
@Table(name = "sys_data_log", schema = "database_distribution")
public class SysDataLog implements IEntity<SysDataLog, Long> {

    @Id
    private Long id;

    @Column(name = "datasource_id")
    private Long datasourceId;

    @Column(name = "database_config_id")
    private Long databaseConfigId;

    @Column(name = "database_name")
    private String databaseName;

    @Column(name = "table_name")
    private String tableName;

    @Column(name = "primary_key")
    private String primaryKey;

    @Column(name = "oper_type")
    private String operType;

    @Column(name = "data_log")
    private String dataLog;

    @JsonFormat(pattern = Consts.TIME_PATTERN)
    @JSONField(format = Consts.TIME_PATTERN)
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = Consts.TIME_PATTERN)
    @JSONField(format = Consts.TIME_PATTERN)
    @Column(name = "update_time")
    private LocalDateTime updateTime;

}
