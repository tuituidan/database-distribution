package com.tuituidan.openhub.mapper;

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

}
