package com.tuituidan.openhub.controller;

import com.tuituidan.openhub.bean.vo.LineData;
import com.tuituidan.openhub.service.HomeService;
import com.tuituidan.tresdin.consts.TresdinConsts;
import java.util.List;
import java.util.Map;
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
    private HomeService homeService;

    /**
     * selectAllDataLogCount
     *
     * @return Long
     */
    @GetMapping("/data_log/count")
    public Long selectAllDataLogCount() {
        return homeService.selectAllDataLogCount();
    }

    /**
     * selectAllPushLogCount
     *
     * @return Long
     */
    @GetMapping("/push_log/count")
    public Long selectAllPushLogCount() {
        return homeService.selectAllPushLogCount();
    }

    /**
     * selectPushLogCount
     *
     * @param status status
     * @return Long
     */
    @GetMapping("/push_log/{status}/count")
    public Long selectPushLogCount(@PathVariable String status) {
        return homeService.selectPushLogCount(status);
    }

    /**
     * todayDataLogLine
     *
     * @return Map
     */
    @GetMapping("/data_log/today/line")
    public Map<String, List<LineData>> todayDataLogLine() {
        return homeService.todayDataLogLine();
    }

    /**
     * todayPushLogLine
     *
     * @return Map
     */
    @GetMapping("/push_log/today/line")
    public Map<String, List<LineData>> todayPushLogLine() {
        return homeService.todayPushLogLine();
    }

    /**
     * lastMonthDataLogLine
     *
     * @return Map
     */
    @GetMapping("/data_log/last_month/line")
    public Map<String, List<LineData>> lastMonthDataLogLine() {
        return homeService.lastMonthDataLogLine();
    }

    /**
     * lastMonthPushLogLine
     *
     * @return Map
     */
    @GetMapping("/push_log/last_month/line")
    public Map<String, List<LineData>> lastMonthPushLogLine() {
        return homeService.lastMonthPushLogLine();
    }

    /**
     * selectTableDataCount
     *
     * @return List
     */
    @GetMapping("/data_log/table/count")
    public List<LineData> selectTableDataCount() {
        return homeService.selectTableDataCount();
    }

    /**
     * selectAppCostTime
     *
     * @return List
     */
    @GetMapping("/push_log/app/cost_time")
    public List<LineData> selectAppCostTime() {
        return homeService.selectAppCostTime();
    }

}
