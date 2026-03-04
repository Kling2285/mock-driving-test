package com.example.utils;

/**
 * 邮件模板工具类（无CodeEmail版本）
 */
public class EmailTemplateUtil {

    /**
     * 生成验证码邮件正文
     * @param verifyCode 验证码
     * @param expireMinutes 有效期（分钟）
     * @return 拼接好的正文内容
     */
    public static String generateVerifyCodeContent(String verifyCode, Integer expireMinutes) {
        // 校验必要参数
        if (verifyCode == null || expireMinutes == null) {
            throw new IllegalArgumentException("验证码或有效期不能为空");
        }
        return String.format(
                "您的验证码是：%s\n该验证码有效期%d分钟，请及时验证，请勿泄露给他人！",
                verifyCode,
                expireMinutes
        );
    }
}