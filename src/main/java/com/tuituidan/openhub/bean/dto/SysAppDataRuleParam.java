package com.tuituidan.openhub.bean.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * SysDataSource.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/8/31
 */
@Getter
@Setter
public class SysAppDataRuleParam {

    private Long appId;

    private Long databaseConfigId;

    private String ruleType;

    private String ruleExp;

}
