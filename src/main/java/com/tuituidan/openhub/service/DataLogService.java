package com.tuituidan.openhub.service;

import com.tuituidan.openhub.bean.entity.SysDataLog;
import com.tuituidan.openhub.bean.entity.SysPushLog;
import com.tuituidan.openhub.bean.vo.SysDataLogView;
import com.tuituidan.openhub.mapper.SysDataLogMapper;
import com.tuituidan.openhub.mapper.SysPushLogMapper;
import com.tuituidan.tresdin.mybatis.QueryHelper;
import com.tuituidan.tresdin.mybatis.bean.PageParam;
import com.tuituidan.tresdin.page.PageData;
import com.tuituidan.tresdin.util.BeanExtUtils;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

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
            return sysPushLogMapper.select(search);
        });
    }

}
