package com.tuituidan.openhub.task;

import com.github.pagehelper.page.PageMethod;
import com.tuituidan.openhub.bean.entity.SysScheduleTaskLog;
import com.tuituidan.openhub.mapper.SysScheduleTaskLogMapper;
import com.tuituidan.tresdin.mybatis.QueryHelper;
import com.tuituidan.tresdin.schedule.task.bean.ScheduleTaskLog;
import com.tuituidan.tresdin.schedule.task.service.IScheduleTaskLogStorage;
import com.tuituidan.tresdin.util.BeanExtUtils;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.util.Assert;

/**
 * ScheduleTaskStorageImpl.
 *
 * @author zhujunhan
 * @version 1.0
 * @date 2026/5/31
 */
// 注释掉，使用默认实现
// @Service
public class ScheduleTaskStorageLogImpl implements IScheduleTaskLogStorage {

    @Resource
    private SysScheduleTaskLogMapper sysScheduleTaskLogMapper;

    @Override
    public List<ScheduleTaskLog> selectTaskLogList(String taskId) {
        PageMethod.startPage(1, 20);
        QueryHelper.orderBy("-start_time_stamp");
        return sysScheduleTaskLogMapper.select(new SysScheduleTaskLog().setTaskId(taskId))
                .stream().map(taskLog -> new ScheduleTaskLog()
                        .setTaskId(taskLog.getTaskId())
                        .setStartTimeStamp(taskLog.getStartTimeStamp())
                        .setSuccess(taskLog.getSuccess())
                        .setMsg(taskLog.getMsg())
                        .setEndTimeStamp(taskLog.getEndTimeStamp())).collect(Collectors.toList());
    }

    @Override
    public void insertTaskLog(ScheduleTaskLog log) {
        SysScheduleTaskLog entity = BeanExtUtils.convert(log, SysScheduleTaskLog::new);
        sysScheduleTaskLogMapper.insertSelective(entity);
    }

    @Override
    public void updateTaskLog(ScheduleTaskLog log) {
        List<SysScheduleTaskLog> logList = sysScheduleTaskLogMapper.select(new SysScheduleTaskLog()
                .setTaskId(log.getTaskId()).setStartTimeStamp(log.getStartTimeStamp()));
        Assert.notEmpty(logList, "未找到对应的任务日志");
        SysScheduleTaskLog entity = logList.get(0);
        entity.setSuccess(log.getSuccess()).setMsg(log.getMsg()).setEndTimeStamp(log.getEndTimeStamp());
        sysScheduleTaskLogMapper.updateByPrimaryKeySelective(entity);
    }

}
