package com.tuituidan.openhub.controller;

import com.tuituidan.openhub.bean.entity.SysEarlyWarningEmail;
import com.tuituidan.openhub.service.EarlyWarningEmailService;
import com.tuituidan.tresdin.consts.TresdinConsts;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * EarlyWarningEmailController.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/15
 */
@RestController
@RequestMapping(TresdinConsts.API_V1 + "/email")
public class EarlyWarningEmailController {

    @Resource
    private EarlyWarningEmailService earlyWarningEmailService;

    /**
     * get
     *
     * @return SysEarlyWarningEmail
     */
    @GetMapping
    public SysEarlyWarningEmail get() {
        return earlyWarningEmailService.get();
    }

    /**
     * save
     *
     * @param config config
     */
    @PostMapping
    public void save(@RequestBody SysEarlyWarningEmail config) {
        earlyWarningEmailService.save(config);
    }

}
