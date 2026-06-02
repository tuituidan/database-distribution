package com.tuituidan.openhub.task;

import com.tuituidan.openhub.bean.entity.SysScheduleTask;
import com.tuituidan.openhub.mapper.SysScheduleTaskMapper;
import com.tuituidan.tresdin.schedule.task.bean.ScheduleTask;
import com.tuituidan.tresdin.schedule.task.consts.JobStatus;
import com.tuituidan.tresdin.schedule.task.service.IScheduleTaskStorage;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.weekend.Weekend;

/**
 * ScheduleTaskStorageImpl.
 *
 * @author zhujunhan
 * @version 1.0
 * @date 2026/5/31
 */
@Service
public class ScheduleTaskStorageImpl implements IScheduleTaskStorage {

    @Resource
    private SysScheduleTaskMapper sysScheduleTaskMapper;

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

}
