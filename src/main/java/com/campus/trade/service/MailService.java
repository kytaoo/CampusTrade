package com.campus.trade.service;

public interface MailService {

    /**
     * 发送简单的文本邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     */
    void sendSimpleMail(String to, String subject, String content);

    /**
     * 发送邮箱验证码 (异步)
     * @param email 目标邮箱
     * @param code 验证码
     */
    void sendVerificationCode(String email, String code);
}