package com.tuituidan.openhub.task;

import com.github.pagehelper.page.PageMethod;
import com.tuituidan.openhub.bean.entity.SysScheduleTask;
import com.tuituidan.openhub.bean.entity.SysScheduleTaskLog;
import com.tuituidan.openhub.mapper.SysScheduleTaskLogMapper;
import com.tuituidan.openhub.mapper.SysScheduleTaskMapper;
import com.tuituidan.tresdin.mybatis.QueryHelper;
import com.tuituidan.tresdin.schedule.task.bean.ScheduleTask;
import com.tuituidan.tresdin.schedule.task.bean.ScheduleTaskLog;
import com.tuituidan.tresdin.schedule.task.consts.JobStatus;
import com.tuituidan.tresdin.schedule.task.service.IScheduleTaskStorage;
import com.tuituidan.tresdin.util.BeanExtUtils;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.Assert;
import tk.mybatis.mapper.weekend.Weekend;

/**
 * ScheduleTaskStorageImpl.
 *
 * @author zhujunhan
 * @version 1.0
 * @date 2026/5/31
 */
// 注释掉，使用默认实现
// @Service
public class ScheduleTaskStorageImpl implements IScheduleTaskStorage {

    @Resource
    private SysScheduleTaskMapper sysScheduleTaskMapper;

    @Resource
    private SysScheduleTaskLogMapper sysScheduleTaskLogMapper;

    @Override
    public List<ScheduleTask> selectTaskList() {
        List<SysScheduleTask> list = sysScheduleTaskMapper.selectAll();
        return list.stream().map(task -> new ScheduleTask()
                        .setTaskFullPath(task.getTaskPath())
                        .setStatus(JobStatus.of(task.getTaskStatus())))
                .collect(Collectors.toList());
    }

    @Override
    public void saveTask(String fullPath, JobStatus status) {
        List<SysScheduleTask> taskList = sysScheduleTaskMapper.select(new SysScheduleTask().setTaskPath(fullPath));
        if (CollectionUtils.isEmpty(taskList)) {
            sysScheduleTaskMapper.insertSelective(new SysScheduleTask()
                    .setTaskPath(fullPath).setTaskStatus(status.getCode()));
        } else {
            SysScheduleTask task = taskList.get(0);
            sysScheduleTaskMapper.updateByPrimaryKeySelective(task.setTaskStatus(status.getCode()));
        }
    }

    @Override
    public void deleteTask(Set<String> fullPaths) {
        Weekend<SysScheduleTask> taskWeekend = Weekend.of(SysScheduleTask.class);
        taskWeekend.weekendCriteria().andIn(SysScheduleTask::getTaskPath, fullPaths);
        sysScheduleTaskMapper.deleteByExample(taskWeekend);
    }

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
