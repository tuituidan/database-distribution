package com.tuituidan.openhub.bean.dto;

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
public class SysDatabaseConfigParam {

    private Long datasourceId;

    private String databaseName;

    private String tableName;

    private String primaryKey;

    private String incrementKey;

    private String incrementType;
}
