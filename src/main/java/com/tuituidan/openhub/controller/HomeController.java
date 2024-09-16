package com.tuituidan.openhub.controller;

import com.github.benmanes.caffeine.cache.Cache;
import com.tuituidan.openhub.bean.entity.SysApp;
import com.tuituidan.openhub.bean.entity.SysDataSource;
import com.tuituidan.openhub.bean.vo.LineData;
import com.tuituidan.openhub.mapper.HomeMapper;
import com.tuituidan.openhub.mapper.SysAppMapper;
import com.tuituidan.openhub.mapper.SysDataSourceMapper;
import com.tuituidan.tresdin.consts.TresdinConsts;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
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

    @Resource
    private Cache<Long, SysApp> sysAppCache;

    @Resource
    private SysAppMapper sysAppMapper;

    @Resource
    private Cache<Long, SysDataSource> sysDataSourceCache;

    @Resource
    private SysDataSourceMapper sysDataSourceMapper;

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

    @GetMapping("/data_log/today/line")
    public Map<String, List<LineData>> todayDataLogLine() {
        Map<Long, String> legendMap = sysDataSourceMapper.selectAll().stream()
                .collect(Collectors.toMap(SysDataSource::getId, SysDataSource::getName));
        List<LineData> lineData = homeMapper.todayDataLogLine(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        return buildLineData(lineData, legendMap, String::valueOf,
                LocalDateTime.now().get(ChronoField.HOUR_OF_DAY));
    }

    @GetMapping("/push_log/today/line")
    public Map<String, List<LineData>> todayPushLogLine() {
        Map<Long, String> legendMap = sysAppMapper.selectAll().stream().collect(Collectors.toMap(SysApp::getId,
                SysApp::getAppName));
        List<LineData> lineData = homeMapper.todayPushLogLine(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        return buildLineData(lineData, legendMap, String::valueOf,
                LocalDateTime.now().get(ChronoField.HOUR_OF_DAY));
    }

    @GetMapping("/data_log/last_month/line")
    public Map<String, List<LineData>> lastMonthDataLogLine() {
        LocalDate firstDay = LocalDate.now().plusDays(-30);
        Map<Long, String> legendMap = sysDataSourceMapper.selectAll().stream()
                .collect(Collectors.toMap(SysDataSource::getId, SysDataSource::getName));
        List<LineData> lineData = homeMapper.lastMonthDataLogLine(firstDay.format(DateTimeFormatter.BASIC_ISO_DATE));
        return buildLineData(lineData, legendMap,
                index -> firstDay.plusDays(index).format(DateTimeFormatter.ofPattern("MM-dd")), 30);
    }

    @GetMapping("/push_log/last_month/line")
    public Map<String, List<LineData>> lastMonthPushLogLine() {
        LocalDate firstDay = LocalDate.now().plusDays(-30);
        Map<Long, String> legendMap = sysAppMapper.selectAll().stream().collect(Collectors.toMap(SysApp::getId,
                SysApp::getAppName));
        List<LineData> lineData = homeMapper.lastMonthPushLogLine(firstDay.format(DateTimeFormatter.BASIC_ISO_DATE));
        return buildLineData(lineData, legendMap,
                index -> firstDay.plusDays(index).format(DateTimeFormatter.ofPattern("MM-dd")), 30);
    }

    private Map<String, List<LineData>> buildLineData(List<LineData> lineData,
            Map<Long, String> legendMap, IntFunction<String> xdataFunc, int length) {
        Map<Long, List<LineData>> dataMap = lineData.stream().collect(Collectors.groupingBy(LineData::getId));
        Map<String, List<LineData>> result = new HashMap<>(dataMap.size());
        for (Entry<Long, String> legendEntry : legendMap.entrySet()) {
            Map<String, Integer> sourceMap = dataMap.getOrDefault(legendEntry.getKey(), new ArrayList<>())
                    .stream().collect(Collectors.toMap(LineData::getXdata, LineData::getYdata));
            List<LineData> distValues = new ArrayList<>();
            for (int i = 0; i <= length; i++) {
                String xdata = xdataFunc.apply(i);
                distValues.add(new LineData()
                        .setXdata(xdata)
                        .setYdata(sourceMap.getOrDefault(xdata, 0)));
            }
            result.put(legendEntry.getValue(), distValues);
        }
        return result;
    }

}
