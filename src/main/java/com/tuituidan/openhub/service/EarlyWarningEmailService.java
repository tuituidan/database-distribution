package com.tuituidan.openhub.service;

import com.tuituidan.openhub.bean.entity.SysEarlyWarningEmail;
import com.tuituidan.openhub.mapper.SysEarlyWarningEmailMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
import javax.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

/**
 * MailService.
 *
 * @author tuituidan
 * @version 1.0
 * @date 2024/9/15
 */
@Service
public class EarlyWarningEmailService implements ApplicationRunner {

    private JavaMailSender javaMailSender;

    @Resource
    private SysEarlyWarningEmailMapper sysEarlyWarningEmailMapper;

    private SysEarlyWarningEmail emailConfig;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<SysEarlyWarningEmail> list = sysEarlyWarningEmailMapper.selectAll();
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        initConfig(list.get(0));
    }

    /**
     * get
     *
     * @return SysEarlyWarningEmail
     */
    public SysEarlyWarningEmail get() {
        List<SysEarlyWarningEmail> list = sysEarlyWarningEmailMapper.selectAll();
        if (CollectionUtils.isEmpty(list)) {
            return new SysEarlyWarningEmail();
        }
        return list.get(0);
    }

    /**
     * save
     *
     * @param config config
     */
    public void save(SysEarlyWarningEmail config) {
        if (config.getId() == null) {
            sysEarlyWarningEmailMapper.insertSelective(config);
        } else {
            sysEarlyWarningEmailMapper.updateByPrimaryKeySelective(config);
        }
        initConfig(config);
    }

    /**
     * send
     *
     * @param message message
     */
    public void send(SimpleMailMessage message) {
        if (javaMailSender == null
                || emailConfig == null
                || ArrayUtils.isEmpty(emailConfig.getReceivers())) {
            return;
        }
        message.setFrom(emailConfig.getUsername());
        message.setTo(emailConfig.getReceivers());
        javaMailSender.send(message);
    }

    private void initConfig(SysEarlyWarningEmail config) {
        this.emailConfig = config;
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getHost());
        sender.setPort(config.getPort());
        sender.setUsername(config.getUsername());
        sender.setPassword(config.getPassword());
        sender.setProtocol(config.getProtocol());
        sender.setDefaultEncoding(StandardCharsets.UTF_8.name());
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.ocketFactoryClass", "javax.net.ssl.SSLSocketFactory");
        sender.setJavaMailProperties(properties);
        this.javaMailSender = sender;
    }

}
