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
 * SysDataSource.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/8/31
 */
@Getter
@Setter
@Accessors(chain = true)
@Table(name = "sys_early_warning_email", schema = "database_distribution")
public class SysEarlyWarningEmail implements IEntity<SysEarlyWarningEmail, Long> {

    private static final long serialVersionUID = -4557224656309498343L;

    @Id
    private Long id;

    private String host;

    private String protocol;

    private Integer port;

    private String username;

    private String password;

    @Column(name = "receivers")
    private String[] receivers;

    @JsonFormat(pattern = Consts.TIME_PATTERN)
    @JSONField(format = Consts.TIME_PATTERN)
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = Consts.TIME_PATTERN)
    @JSONField(format = Consts.TIME_PATTERN)
    @Column(name = "update_time")
    private LocalDateTime updateTime;

}
