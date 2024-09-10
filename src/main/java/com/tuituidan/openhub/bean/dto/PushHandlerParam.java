package com.tuituidan.openhub.bean.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * PushHandlerParam.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/11
 */
@Getter
@Setter
public class PushHandlerParam {

    private Long[] ids;

    private Long datasourceId;

    private String incrementValue;

}
