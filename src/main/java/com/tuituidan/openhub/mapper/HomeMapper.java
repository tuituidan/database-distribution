package com.tuituidan.openhub.mapper;

import com.tuituidan.openhub.bean.vo.LineData;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * HomeMapper.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/8/31
 */
public interface HomeMapper {

    /**
     * selectAllDataLogCount
     *
     * @return Long
     */
    Long selectAllDataLogCount();

    /**
     * selectAllPushLogCount
     *
     * @return Long
     */
    Long selectAllPushLogCount();

    /**
     * selectPushLogCount
     *
     * @param status status
     * @param date date
     * @return Long
     */
    Long selectPushLogCount(@Param("status") String status, @Param("date") String date);

    /**
     * todayDataLogLine
     *
     * @param curDate curDate
     * @return List
     */
    List<LineData> todayDataLogLine(@Param("curDate") String curDate);

    /**
     * todayPushLogLine
     *
     * @param curDate curDate
     * @return List
     */
    List<LineData> todayPushLogLine(@Param("curDate") String curDate);

    /**
     * lastMonthDataLogLine
     *
     * @param lastMonthDate lastMonthDate
     * @return List
     */
    List<LineData> lastMonthDataLogLine(@Param("lastMonthDate") String lastMonthDate);

    /**
     * lastMonthPushLogLine
     *
     * @param lastMonthDate lastMonthDate
     * @return List
     */
    List<LineData> lastMonthPushLogLine(@Param("lastMonthDate") String lastMonthDate);

    /**
     * selectTableDataCount
     *
     * @return List
     */
    List<LineData> selectTableDataCount();

    /**
     * selectAppCostTime
     *
     * @return List
     */
    List<LineData> selectAppCostTime();

    /**
     * selectOperType
     *
     * @return List
     */
    List<LineData> selectOperType();

}
