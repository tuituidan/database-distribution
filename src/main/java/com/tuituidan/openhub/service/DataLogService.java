package com.tuituidan.openhub.service;

import com.tuituidan.openhub.bean.entity.SysDataLog;
import com.tuituidan.openhub.bean.entity.SysPushLog;
import com.tuituidan.openhub.bean.vo.SysDataLogView;
import com.tuituidan.openhub.consts.enums.PushStatusEnum;
import com.tuituidan.openhub.mapper.SysDataLogMapper;
import com.tuituidan.openhub.mapper.SysPushLogMapper;
import com.tuituidan.tresdin.mybatis.QueryHelper;
import com.tuituidan.tresdin.mybatis.bean.PageParam;
import com.tuituidan.tresdin.page.PageData;
import com.tuituidan.tresdin.util.BeanExtUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.weekend.Weekend;
import tk.mybatis.mapper.weekend.WeekendCriteria;

/**
 * DataLogService.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/7
 */
@Service
public class DataLogService {

    @Resource
    private SysDataLogMapper sysDataLogMapper;

    @Resource
    private SysPushLogMapper sysPushLogMapper;

    @Resource
    private DataAnalyseService dataAnalyseService;

    /**
     * selectDataLogPage
     *
     * @param pageParam pageParam
     * @param search search
     * @return PageData
     */
    public PageData<List<SysDataLogView>> selectDataLogPage(PageParam pageParam, SysDataLog search) {
        PageData<List<SysDataLog>> pageData = QueryHelper.queryPage(pageParam.getOffset(), pageParam.getLimit(),
                () -> {
                    QueryHelper.orderBy(pageParam.getSort(), SysDataLog.class);
                    return sysDataLogMapper.select(search);
                });
        return QueryHelper.mapPage(pageData, item -> BeanExtUtils.convert(item, SysDataLogView::new));
    }

    /**
     * getPushLogByDataLogId
     *
     * @param dataLogId dataLogId
     * @return List
     */
    public List<SysPushLog> getPushLogByDataLogId(Long dataLogId) {
        return sysPushLogMapper.select(new SysPushLog().setDataLogId(dataLogId));
    }

    /**
     * selectPushPage
     *
     * @param pageParam pageParam
     * @param search search
     * @return PageData
     */
    public PageData<List<SysPushLog>> selectPushPage(PageParam pageParam, SysPushLog search) {
        return QueryHelper.queryPage(pageParam.getOffset(), pageParam.getLimit(), () -> {
            QueryHelper.orderBy(pageParam.getSort(), SysPushLog.class);
            return sysPushLogMapper.selectByExample(buildWeekend(search));
        });
    }

    /**
     * getFailPushLogIds
     *
     * @return List
     */
    public List<Long> getFailPushLogIds() {
        Weekend<SysPushLog> search = buildWeekend(new SysPushLog().setStatus(PushStatusEnum.FAIL.getCode()));
        return sysPushLogMapper.selectByExample(search).stream().map(SysPushLog::getId).collect(Collectors.toList());
    }

    private Weekend<SysPushLog> buildWeekend(SysPushLog search) {
        Weekend<SysPushLog> weekend = Weekend.of(SysPushLog.class);
        WeekendCriteria<SysPushLog, Object> criteria = weekend.weekendCriteria();
        if (search.getAppId() != null) {
            criteria.andEqualTo(SysPushLog::getAppId, search.getAppId());
        }
        if (search.getDataLogId() != null) {
            criteria.andEqualTo(SysPushLog::getDataLogId, search.getDataLogId());
        }
        if (StringUtils.isNotBlank(search.getStatus())) {
            criteria.andEqualTo(SysPushLog::getStatus, search.getStatus());
        }
        return weekend;
    }

    /**
     * pushLog
     *
     * @param id id
     */
    public void pushLog(Long id) {
        pushLog(sysPushLogMapper.selectByPrimaryKey(id));
    }

    /**
     * pushLog
     *
     * @param pushLog pushLog
     */
    public void pushLog(SysPushLog pushLog) {
        SysDataLog dataLog = sysDataLogMapper.selectByPrimaryKey(pushLog.getDataLogId());
        long startTime = System.currentTimeMillis();
        pushLog.setPushTime(LocalDateTime.now());
        dataAnalyseService.analyse(dataLog, pushLog);
        pushLog.setCostTime(System.currentTimeMillis() - startTime);
        pushLog.setPushTimes(pushLog.getPushTimes() + 1);
        sysPushLogMapper.updateByPrimaryKeySelective(pushLog);
    }

}
