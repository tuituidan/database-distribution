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
 * SysScheduleTask.
 *
 * @author zhujunhan
 * @version 1.0
 * @date 2026/5/31
 */
@Getter
@Setter
@Accessors(chain = true)
@Table(name = "sys_schedule_task_log", schema = "database_distribution")
public class SysScheduleTaskLog implements IEntity<SysScheduleTaskLog, Long> {

    private static final long serialVersionUID = 4056285341784038332L;

    @Id
    private Long id;

    private String taskId;

    private Long startTimeStamp;

    private Boolean success;

    private String msg;

    private Long endTimeStamp;

    @JsonFormat(pattern = Consts.TIME_PATTERN)
    @JSONField(format = Consts.TIME_PATTERN)
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = Consts.TIME_PATTERN)
    @JSONField(format = Consts.TIME_PATTERN)
    @Column(name = "update_time")
    private LocalDateTime updateTime;

}
