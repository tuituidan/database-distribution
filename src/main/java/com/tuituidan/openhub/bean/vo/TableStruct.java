package com.tuituidan.openhub.bean.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * TableStruct.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/1
 */
@Getter
@Setter
@Accessors(chain = true)
public class TableStruct {

    private String tableSchema;

    private String tableName;

    private String columnName;

    private Integer ordinalPosition;

}

