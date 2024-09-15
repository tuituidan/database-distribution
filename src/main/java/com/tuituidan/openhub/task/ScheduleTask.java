package com.tuituidan.openhub.task;

import com.tuituidan.openhub.service.EarlyWarningEmailService;
import com.tuituidan.tresdin.schedule.task.annotation.TaskName;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
    /**
     * schedule
     */
    @TaskName("定时推送失败日志1")
    @Scheduled(cron = "0 0/2 * * * ?")
    public void schedule() {
        log.info("执行1");
    }

    /**
     * schedule
     */
    @TaskName("定时推送失败日志2")
    @Scheduled(cron = "0 0/3 * * * ?")
    public void schedule1() {
        log.info("执行2");
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("测试邮件");
        message.setText("测试邮件内容");
        //earlyWarningEmailService.send(message);
    }

}
