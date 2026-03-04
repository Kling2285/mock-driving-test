package com.example.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 重构后的BCrypt加密工具类（避免静态初始化失败）
 */
public class PasswordEncoderUtil {
    // 工作因子（12是平衡值）
    private static final int STRENGTH = 12;
    // 懒加载：使用时才初始化
    private static PasswordEncoder encoder;

    // 私有化构造
    private PasswordEncoderUtil() {}

    // 初始化编码器（懒加载）
    private static PasswordEncoder getEncoder() {
        if (encoder == null) {
            encoder = new BCryptPasswordEncoder(STRENGTH);
        }
        return encoder;
    }

    /**
     * 加密密码
     */
    public static String encodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        return getEncoder().encode(rawPassword);
    }

    /**
     * 验证密码
     */
    public static boolean matchPassword(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return getEncoder().matches(rawPassword, encodedPassword);
    }
}