package com.example.common;

import java.util.Random;

// 验证码生成工具类（仅生成6位纯数字验证码）
public class VerifyCodeGenerator {
    // 私有化构造方法，禁止外部实例化（工具类规范）
    private VerifyCodeGenerator() {}

    // 生成6位纯数字验证码（核心方法）
    public static String generate6DigitCode() {
        Random random = new Random();
        // 生成 100000 ~ 999999 之间的随机数，确保是6位数字
        int codeNum = random.nextInt(900000) + 100000;
        return String.valueOf(codeNum);
    }
}