package com.tuituidan.openhub.consts;

import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Consts.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/10
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Consts {

    /**
     * TIME_PATTERN
     */
    public static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * TIME_FORMATTER
     */
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATTERN);

}
