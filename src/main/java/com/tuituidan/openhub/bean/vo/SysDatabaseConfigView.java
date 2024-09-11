package com.tuituidan.openhub.bean.vo;

import java.time.LocalDateTime;
import java.util.List;
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
public class SysDatabaseConfigView {

    private Long id;

    private Long datasourceId;

    private String databaseName;

    private String tableName;

    private String tableComment;

    private String primaryKey;

    private String incrementKey;

    private String incrementType;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private List<TableStruct> tableStruct;

}
