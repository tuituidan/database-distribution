package com.tuituidan.openhub.consts.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * StatusEnum.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/8
 */
@Getter
@AllArgsConstructor
public enum FilterTypeEnum {
    /**
     * 表达式过滤
     */
    EXP("01", "表达式过滤"),
    SQL("02", "SQL过滤");

    private String code;

    private String name;
}
