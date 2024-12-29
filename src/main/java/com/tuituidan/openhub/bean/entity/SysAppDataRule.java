package com.tuituidan.openhub.bean.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.tuituidan.openhub.consts.Consts;
import com.tuituidan.tresdin.datatranslate.translator.dict.DictType;
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
@Table(name = "sys_app_data_rule", schema = "database_distribution")
public class SysAppDataRule implements IEntity<SysAppDataRule, Long> {

    private static final long serialVersionUID = -17095764960230242L;

    @Id
    private Long id;

    @Column(name = "app_id")
    private Long appId;

    @Column(name = "database_config_id")
    private Long databaseConfigId;

    @Column(name = "rule_type")
    @DictType("2000000005")
    private String ruleType;

    @Column(name = "rule_exp")
    private String ruleExp;

    @JsonFormat(pattern = Consts.TIME_PATTERN)
    @JSONField(format = Consts.TIME_PATTERN)
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = Consts.TIME_PATTERN)
    @JSONField(format = Consts.TIME_PATTERN)
    @Column(name = "update_time")
    private LocalDateTime updateTime;

}
