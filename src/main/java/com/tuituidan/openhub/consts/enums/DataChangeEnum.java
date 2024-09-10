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
     * 时间
     */
    REPLACE("replace", "覆盖");

    private String code;

    private String name;
}
