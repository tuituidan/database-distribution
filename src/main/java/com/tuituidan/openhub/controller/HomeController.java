package com.tuituidan.openhub.controller;

import com.tuituidan.openhub.mapper.HomeMapper;
import com.tuituidan.tresdin.consts.TresdinConsts;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * HomeController.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/13
 */
@RestController
@RequestMapping(TresdinConsts.API_V1 + "/home")
public class HomeController {

    @Resource
    private HomeMapper homeMapper;

    @GetMapping("/data_log/count")
    public Long selectAllDataLogCount() {
        return homeMapper.selectAllDataLogCount();
    }

    @GetMapping("/push_log/count")
    public Long selectAllPushLogCount() {
        return homeMapper.selectAllPushLogCount();
    }

    @GetMapping("/push_log/{status}/count")
    public Long selectPushLogCount(@PathVariable String status) {
        return homeMapper.selectPushLogCount(status, LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
    }

}
