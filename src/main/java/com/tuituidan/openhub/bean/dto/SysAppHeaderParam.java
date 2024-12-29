package com.tuituidan.openhub.bean.dto;

import java.io.Serializable;
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
public class SysAppHeaderParam implements Serializable {

    private static final long serialVersionUID = 4731639540002448751L;

    private String type;

    private String key;

    private String value;

}
