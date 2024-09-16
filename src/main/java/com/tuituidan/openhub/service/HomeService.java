package com.tuituidan.openhub.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.tuituidan.openhub.bean.entity.SysApp;
import com.tuituidan.openhub.bean.entity.SysDataSource;
import com.tuituidan.openhub.bean.vo.LineData;
import com.tuituidan.openhub.mapper.HomeMapper;
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
import org.springframework.stereotype.Service;

/**
 * HomeService.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/16
 */
@Service
public class HomeService {

    @Resource
    private HomeMapper homeMapper;

    @Resource
    private Cache<Long, SysApp> sysAppCache;

    @Resource
    private Cache<Long, SysDataSource> sysDataSourceCache;

    /**
     * selectAllDataLogCount
     *
     * @return Long
     */
    public Long selectAllDataLogCount() {
        return homeMapper.selectAllDataLogCount();
    }

    /**
     * selectAllPushLogCount
     *
     * @return Long
     */
    public Long selectAllPushLogCount() {
        return homeMapper.selectAllPushLogCount();
    }

    /**
     * selectPushLogCount
     *
     * @param status status
     * @return Long
     */
    public Long selectPushLogCount(String status) {
        return homeMapper.selectPushLogCount(status, LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
    }

    /**
     * todayDataLogLine
     *
     * @return Map
     */
    public Map<String, List<LineData>> todayDataLogLine() {
        Map<Long, String> legendMap = sysDataSourceCache.asMap().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getName()));
        List<LineData> lineData = homeMapper.todayDataLogLine(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        return buildLineData(lineData, legendMap, String::valueOf,
                LocalDateTime.now().get(ChronoField.HOUR_OF_DAY));
    }

    /**
     * todayPushLogLine
     *
     * @return Map
     */
    public Map<String, List<LineData>> todayPushLogLine() {
        Map<Long, String> legendMap = sysAppCache.asMap().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getAppName()));
        List<LineData> lineData = homeMapper.todayPushLogLine(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
        return buildLineData(lineData, legendMap, String::valueOf,
                LocalDateTime.now().get(ChronoField.HOUR_OF_DAY));
    }

    /**
     * lastMonthDataLogLine
     *
     * @return Map
     */
    public Map<String, List<LineData>> lastMonthDataLogLine() {
        LocalDate firstDay = LocalDate.now().plusDays(-30);
        Map<Long, String> legendMap = sysDataSourceCache.asMap().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getName()));
        List<LineData> lineData = homeMapper.lastMonthDataLogLine(firstDay.format(DateTimeFormatter.BASIC_ISO_DATE));
        return buildLineData(lineData, legendMap,
                index -> firstDay.plusDays(index).format(DateTimeFormatter.ofPattern("MM-dd")), 30);
    }

    /**
     * lastMonthPushLogLine
     *
     * @return Map
     */
    public Map<String, List<LineData>> lastMonthPushLogLine() {
        LocalDate firstDay = LocalDate.now().plusDays(-30);
        Map<Long, String> legendMap = sysAppCache.asMap().entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> entry.getValue().getAppName()));
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
