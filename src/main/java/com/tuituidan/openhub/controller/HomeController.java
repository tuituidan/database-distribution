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
import java.util.Optional;
import java.util.function.LongFunction;
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
        List<LineData> lineData = homeMapper.todayDataLogLine(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        return buildLineData(lineData, id -> Optional.ofNullable(sysDataSourceCache.get(id,
                                key -> sysDataSourceMapper.selectByPrimaryKey(key)))
                        .map(SysDataSource::getName).orElse(String.valueOf(id)),
                LocalDateTime.now().get(ChronoField.HOUR_OF_DAY));
    }

    @GetMapping("/push_log/today/line")
    public Map<String, List<LineData>> todayPushLogLine() {
        List<LineData> lineData = homeMapper.todayPushLogLine(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        return buildLineData(lineData, id -> Optional.ofNullable(sysAppCache.get(id,
                                key -> sysAppMapper.selectByPrimaryKey(key)))
                        .map(SysApp::getAppName).orElse(String.valueOf(id)),
                LocalDateTime.now().get(ChronoField.HOUR_OF_DAY));
    }

    @GetMapping("/data_log/last_month/line")
    public Map<String, List<LineData>> lastMonthDataLogLine() {
        List<LineData> lineData = homeMapper.lastMonthDataLogLine(LocalDate.now().plusDays(-30)
                .format(DateTimeFormatter.BASIC_ISO_DATE));
        return buildLineData(lineData, id -> Optional.ofNullable(sysDataSourceCache.get(id,
                        key -> sysDataSourceMapper.selectByPrimaryKey(key)))
                .map(SysDataSource::getName).orElse(String.valueOf(id)), 30);
    }

    @GetMapping("/push_log/last_month/line")
    public Map<String, List<LineData>> lastMonthPushLogLine() {
        List<LineData> lineData =
                homeMapper.lastMonthPushLogLine(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        return buildLineData(lineData, id -> Optional.ofNullable(sysAppCache.get(id,
                        key -> sysAppMapper.selectByPrimaryKey(key)))
                .map(SysApp::getAppName).orElse(String.valueOf(id)), 30);
    }

    private Map<String, List<LineData>> buildLineData(List<LineData> lineData,
            LongFunction<String> transNameFunc, Integer length) {
        Map<Long, List<LineData>> dataMap = lineData.stream().collect(Collectors.groupingBy(LineData::getId));
        Map<String, List<LineData>> result = new HashMap<>(dataMap.size());
        for (Entry<Long, List<LineData>> entry : dataMap.entrySet()) {
            Map<Integer, Integer> valueMap = entry.getValue().stream().collect(Collectors.toMap(LineData::getXdata,
                    LineData::getYdata));
            List<LineData> valueList = new ArrayList<>();
            for (int i = 0; i <= length; i++) {
                valueList.add(new LineData().setXdata(i).setYdata(valueMap.getOrDefault(i, 0)));
            }
            result.put(transNameFunc.apply(entry.getKey()), valueList);
        }
        return result;
    }

}
