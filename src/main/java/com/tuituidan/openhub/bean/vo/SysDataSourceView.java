package com.tuituidan.openhub.bean.vo;

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
public class SysDataSourceView {

    private Long id;

    private String name;

    private String host;

    private Integer port;

    private String username;

    private String password;

    private Long serverId;

}
