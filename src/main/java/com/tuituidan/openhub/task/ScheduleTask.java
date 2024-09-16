package com.tuituidan.openhub.task;

import com.tuituidan.openhub.bean.entity.SysDataLog;
import com.tuituidan.openhub.bean.entity.SysPushLog;
import com.tuituidan.openhub.consts.enums.HttpStatusEnum;
import com.tuituidan.openhub.mapper.SysDataLogMapper;
import com.tuituidan.openhub.mapper.SysPushLogMapper;
import com.tuituidan.openhub.service.DataLogService;
import com.tuituidan.openhub.service.EarlyWarningEmailService;
import com.tuituidan.tresdin.mybatis.QueryHelper;
import com.tuituidan.tresdin.page.PageData;
import com.tuituidan.tresdin.schedule.task.annotation.TaskName;
import com.tuituidan.tresdin.util.StringExtUtils;
import java.time.LocalDate;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.weekend.Weekend;

/**
 * ScheduleTaskController.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/15
 */
@Component
@Slf4j
public class ScheduleTask {

    @Resource
    private EarlyWarningEmailService earlyWarningEmailService;

    @Resource
    private SysDataLogMapper sysDataLogMapper;

    @Resource
    private SysPushLogMapper sysPushLogMapper;

    @Resource
    private DataLogService dataLogService;

    /**
     * rePushData
     */
    @TaskName("定时重新推送失败数据")
    @Scheduled(cron = "0 0/30 * * * ?")
    public void rePushData() {
        Weekend<SysPushLog> pushLogWeekend = Weekend.of(SysPushLog.class);
        pushLogWeekend.weekendCriteria()
                .andNotEqualTo(SysPushLog::getStatus, HttpStatusEnum.OK.getCode())
                .andLessThan(SysPushLog::getPushTimes, 5);
        int count = sysPushLogMapper.selectCountByExample(pushLogWeekend);
        if (count <= 0) {
            return;
        }
        // 分页查出来重推，避免数据量过大
        int limit = 100;
        int pageCount = QueryHelper.calcPageCount(count, limit);
        for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
            PageData<List<SysPushLog>> pageData = QueryHelper.queryPage(pageIndex * limit, limit,
                    () -> sysPushLogMapper.selectByExample(pushLogWeekend));
            for (SysPushLog pushLog : pageData.getData()) {
                dataLogService.pushLog(pushLog);
            }
        }
    }

    /**
     * sendEmail
     */
    @TaskName("定时检查推送失败五次以上的数据发送邮件通知")
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void sendEmail() {
        Weekend<SysPushLog> pushLogWeekend = Weekend.of(SysPushLog.class);
        pushLogWeekend.weekendCriteria()
                .andNotEqualTo(SysPushLog::getStatus, HttpStatusEnum.OK.getCode())
                .andGreaterThanOrEqualTo(SysPushLog::getPushTimes, 5);
        int count = sysPushLogMapper.selectCountByExample(pushLogWeekend);
        if (count > 0) {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setSubject("数据分发服务异常通知");
            message.setText(StringExtUtils.format("目前存在{}个推送数据，推送超过五次仍然失败", count));
            earlyWarningEmailService.send(message);
        }
    }

    /**
     * clearOldLog
     */
    @TaskName("定时清理两个月之前的数据日志")
    @Scheduled(cron = "0 0 1 * * ?")
    public void clearOldLog() {
        Weekend<SysPushLog> pushLogWeekend = Weekend.of(SysPushLog.class);
        pushLogWeekend.weekendCriteria().andLessThan(SysPushLog::getPushTime, LocalDate.now().plusMonths(-2));
        sysPushLogMapper.deleteByExample(pushLogWeekend);

        Weekend<SysDataLog> dataLogWeekend = Weekend.of(SysDataLog.class);
        dataLogWeekend.weekendCriteria().andLessThan(SysDataLog::getCreateTime, LocalDate.now().plusMonths(-2));
        sysDataLogMapper.deleteByExample(dataLogWeekend);
    }

}