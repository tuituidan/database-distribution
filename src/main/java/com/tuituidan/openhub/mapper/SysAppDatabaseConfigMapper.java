package com.tuituidan.openhub.mapper;

import com.tuituidan.openhub.bean.entity.SysAppDatabaseConfig;
import com.tuituidan.tresdin.mybatis.mapper.BaseExtMapper;
import java.util.Set;
import org.apache.ibatis.annotations.Param;

/**
 * SysAppMapper.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/8/31
 */
public interface SysAppDatabaseConfigMapper extends BaseExtMapper<SysAppDatabaseConfig> {

    /**
     * deleteByAppIdAndConfigIds
     *
     * @param appId appId
     * @param configIds configIds
     */
    void deleteByAppIdAndConfigIds(@Param("appId") Long appId, @Param("ids") Set<Long> configIds);

}
