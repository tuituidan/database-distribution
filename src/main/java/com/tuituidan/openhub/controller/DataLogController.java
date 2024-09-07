package com.tuituidan.openhub.controller;

import com.tuituidan.openhub.bean.entity.SysDataLog;
import com.tuituidan.openhub.bean.entity.SysPushLog;
import com.tuituidan.openhub.service.DataLogService;
import com.tuituidan.tresdin.consts.TresdinConsts;
import com.tuituidan.tresdin.datatranslate.annotation.DataTranslate;
import com.tuituidan.tresdin.mybatis.bean.PageParam;
import com.tuituidan.tresdin.page.PageData;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DataLogController.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/7
 */
@RestController
@RequestMapping(TresdinConsts.API_V1)
public class DataLogController {

    @Resource
    private DataLogService dataLogService;

    /**
     * selectDataLogPage
     *
     * @param pageParam pageParam
     * @param search search
     * @return PageData
     */
    @GetMapping("/data_log")
    @DataTranslate
    public PageData<List<SysDataLog>> selectDataLogPage(PageParam pageParam, SysDataLog search) {
        return dataLogService.selectDataLogPage(pageParam, search);
    }

    /**
     * getPushLogByDataLogId
     *
     * @param dataLogId dataLogId
     * @return List
     */
    @GetMapping("/data_log/{dataLogId}/push_log")
    @DataTranslate
    public List<SysPushLog> getPushLogByDataLogId(@PathVariable Long dataLogId) {
        return dataLogService.getPushLogByDataLogId(dataLogId);
    }

    /**
     * selectPushPage
     *
     * @param pageParam pageParam
     * @param search search
     * @return PageData
     */
    @GetMapping("/push_log")
    @DataTranslate
    public PageData<List<SysPushLog>> selectPushPage(PageParam pageParam, SysPushLog search) {
        return dataLogService.selectPushPage(pageParam, search);
    }

}
