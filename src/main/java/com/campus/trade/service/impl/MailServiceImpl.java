package com.campus.trade.service.impl;

import com.campus.trade.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MailServiceImpl implements MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from}") // 从配置文件读取发件人
    private String from;

    @Override
    public void sendSimpleMail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        try {
            mailSender.send(message);
            log.info("简单邮件已经发送。收件人: {}, 主题: {}", to, subject);
        } catch (MailException e) {
            log.error("发送简单邮件时发生异常！收件人: {}, 主题: {}", to, subject, e);
        }
    }

    @Override
    @Async // 使用异步执行，需要启动类上添加 @EnableAsync
    public void sendVerificationCode(String email, String code) {
        String subject = "【校园二手交易平台】邮箱验证码";
        String content = "您好！\n\n感谢您注册校园二手交易平台。\n您的邮箱验证码是：" + code + "\n\n请在 5 分钟内使用此验证码完成验证。\n\n如果不是您本人操作，请忽略此邮件。\n\n校园二手交易平台";
        sendSimpleMail(email, subject, content);
        log.info("验证码邮件已异步发送至 {}", email);
    }
}