package com.tuituidan.openhub.consts.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DataChangeEnum.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/10
 */
@Getter
@AllArgsConstructor
public enum DataChangeEnum {
    /**
     * 手动
     */
    REPLACE("replace", "手动"),
    INSERT("insert", "插入"),
    UPDATE("update", "修改"),
    DELETE("delete", "删除");

    private String code;

    private String name;
}
