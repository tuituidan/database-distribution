package com.tuituidan.openhub.bean.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * LineData.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/15
 */
@Getter
@Setter
@Accessors(chain = true)
public class LineData {

    private Long id;

    private String name;

    private Integer xdata;

    private Integer ydata;

}
