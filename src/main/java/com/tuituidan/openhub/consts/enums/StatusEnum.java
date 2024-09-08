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
public enum StatusEnum {
    /**
     * 已启用
     */
    OPEN("01", "已启用"),
    STOP("02", "已禁用");

    private String code;

    private String name;
}
