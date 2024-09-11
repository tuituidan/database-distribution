package com.tuituidan.openhub.service;

import com.tuituidan.openhub.bean.entity.SysDataLog;
import com.tuituidan.openhub.bean.entity.SysPushLog;
import com.tuituidan.openhub.bean.vo.SysDataLogView;
import com.tuituidan.openhub.consts.enums.HttpStatusEnum;
import com.tuituidan.openhub.mapper.SysDataLogMapper;
import com.tuituidan.openhub.mapper.SysPushLogMapper;
import com.tuituidan.tresdin.mybatis.QueryHelper;
import com.tuituidan.tresdin.mybatis.bean.PageParam;
import com.tuituidan.tresdin.page.PageData;
import com.tuituidan.tresdin.util.BeanExtUtils;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;
import tk.mybatis.mapper.entity.Example.Criteria;
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
            Weekend<SysPushLog> weekend = Weekend.of(SysPushLog.class);
            WeekendCriteria<SysPushLog, Object> criteria = weekend.weekendCriteria();
            if (search.getAppId() != null) {
                criteria.andEqualTo(SysPushLog::getAppId, search.getAppId());
            }
            if (search.getDataLogId() != null) {
                criteria.andEqualTo(SysPushLog::getDataLogId, search.getDataLogId());
            }
            if (StringUtils.isNotBlank(search.getStatus())) {
                if (HttpStatusEnum.OK.getCode().equals(search.getStatus())) {
                    criteria.andEqualTo(SysPushLog::getStatus, HttpStatusEnum.OK.getCode());
                } else {
                    criteria.andNotEqualTo(SysPushLog::getStatus, HttpStatusEnum.OK.getCode());
                }
            }
            return sysPushLogMapper.selectByExample(weekend);
        });
    }

    /**
     * getFailPushLogIds
     *
     * @return List
     */
    public List<Long> getFailPushLogIds() {
        Example example = new Example(SysPushLog.class);
        Criteria criteria = example.createCriteria();
        criteria.andNotEqualTo("status", HttpStatusEnum.OK.getCode());
        return sysPushLogMapper.selectByExample(example).stream().map(SysPushLog::getId).collect(Collectors.toList());
    }

    /**
     * pushLog
     *
     * @param id id
     */
    public void pushLog(Long id) {
        SysPushLog pushLog = sysPushLogMapper.selectByPrimaryKey(id);
        SysDataLog dataLog = sysDataLogMapper.selectByPrimaryKey(pushLog.getDataLogId());

    }

}
