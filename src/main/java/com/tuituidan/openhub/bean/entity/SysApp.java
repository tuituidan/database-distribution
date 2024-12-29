package com.tuituidan.openhub.bean.entity;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.tuituidan.openhub.bean.dto.SysAppHeaderParam;
import com.tuituidan.openhub.config.mybatis.AppHeaderTypeHandler;
import com.tuituidan.openhub.consts.Consts;
import com.tuituidan.tresdin.mybatis.bean.IEntity;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import tk.mybatis.mapper.annotation.ColumnType;

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
@Table(name = "sys_app", schema = "database_distribution")
public class SysApp implements IEntity<SysApp, Long> {

    private static final long serialVersionUID = 3735281826773538891L;

    @Id
    private Long id;

    @Column(name = "app_key")
    private String appKey;

    @Column(name = "app_secret")
    private String appSecret;

    @Column(name = "app_name")
    private String appName;

    private String url;

    @Column(name = "result_exp")
    private String resultExp;

    @ColumnType(typeHandler = AppHeaderTypeHandler.class)
    private List<SysAppHeaderParam> headers;

    @JsonFormat(pattern = Consts.TIME_PATTERN)
    @JSONField(format = Consts.TIME_PATTERN)
    @Column(name = "create_time")
    private LocalDateTime createTime;

    @JsonFormat(pattern = Consts.TIME_PATTERN)
    @JSONField(format = Consts.TIME_PATTERN)
    @Column(name = "update_time")
    private LocalDateTime updateTime;

}
