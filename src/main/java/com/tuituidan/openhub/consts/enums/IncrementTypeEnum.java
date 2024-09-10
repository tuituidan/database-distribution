package com.tuituidan.openhub.consts.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * IncrementTypeEnum.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/10
 */
@Getter
@AllArgsConstructor
public enum IncrementTypeEnum {
    /**
     * 时间
     */
    DATE("date", "时间"),
    NUMBER("number", "数字");

    private String code;

    private String name;
}
