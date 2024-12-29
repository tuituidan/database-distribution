package com.tuituidan.openhub.bean.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * SysApp.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/8/31
 */
@Getter
@Setter
public class SysAppParam {

    private String appKey;

    private String appSecret;

    private String appName;

    private String url;

    private String resultExp;

    private List<SysAppHeaderParam> headers;

}
