package com.tuituidan.openhub.controller;

import com.tuituidan.openhub.consts.Consts;
import com.tuituidan.tresdin.consts.TresdinConsts;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HealthController.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/12
 */
@RestController
@RequestMapping(TresdinConsts.API_V1 + "health")
public class HealthController {

    /**
     * health
     */
    @GetMapping
    public void health() {
        // 无实现
    }

    /**
     * healthTime
     */
    @GetMapping("/time")
    public String healthTime() {
        return LocalDateTime.now().format(Consts.TIME_FORMATTER);
    }

}
