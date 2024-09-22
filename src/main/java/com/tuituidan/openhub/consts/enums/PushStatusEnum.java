package com.tuituidan.openhub.consts.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * PushStatusEnum.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/11
 */
@Getter
@AllArgsConstructor
public enum PushStatusEnum {
    /**
     * 成功
     */
    SUCCESS("01", "成功"),
    FAIL("02", "失败");

    private String code;

    private String name;
}
