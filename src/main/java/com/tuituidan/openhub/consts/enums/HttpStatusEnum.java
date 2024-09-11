package com.tuituidan.openhub.consts.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * HttpStatusEnum.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/11
 */
@Getter
@AllArgsConstructor
public enum HttpStatusEnum {
    /**
     * 成功
     */
    OK("200", "成功"),
    FAIL("500", "失败");

    private String code;

    private String name;
}
