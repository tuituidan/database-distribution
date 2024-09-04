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
 * SysApp.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/8/31
 */
@Getter
@Setter
@Accessors(chain = true)
@Table(name = "sys_push_log", schema = "database_distribution")
public class SysPushLog implements IEntity<SysPushLog, Long> {

    @Id
    private Long id;

    @Column(name = "data_log_id")
    private Long dataLogId;

    @Column(name = "app_id")
    private Long appId;

    private String status;

    private String response;

    @Column(name = "push_time")
    private LocalDateTime pushTime;

    @Column(name = "cost_time")
    private Long costTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "update_time")
    private LocalDateTime updateTime;

}