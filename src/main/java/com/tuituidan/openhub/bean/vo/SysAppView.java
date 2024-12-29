package com.tuituidan.openhub.bean.vo;

import com.tuituidan.openhub.bean.dto.SysAppHeaderParam;
import com.tuituidan.openhub.bean.entity.SysAppDataRule;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * SysApp.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/8/31
 */
@Getter
@Setter
@Accessors(chain = true)
public class SysAppView {

    private Long id;

    private String appKey;

    private String appSecret;

    private String appName;

    private String url;

    private String resultExp;

    private List<SysAppHeaderParam> headers;
    
    private List<SysAppDataRule> dataRules;
}
