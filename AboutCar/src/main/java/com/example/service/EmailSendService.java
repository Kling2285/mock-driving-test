package com.example.service;

/**
 * 邮件发送 Service 接口
 */
public interface EmailSendService {

    /**
     * 发送验证码邮件
     * @param toEmail 收件人邮箱
     */
    void sendVerifyCodeEmail(String toEmail);

    /**
     * 校验验证码有效性
     * @param email 收件人邮箱
     * @param inputCode 用户输入的验证码
     * @return 校验结果（true=有效，false=无效）
     */
    boolean checkVerifyCode(String email, String inputCode);
}
