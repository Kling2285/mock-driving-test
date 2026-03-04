package com.example.utils;

import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;

import java.util.Date;

public class TokenUtils {
    // 与 JWTInterceptor 中的固定密钥保持一致
    private static final String FIXED_SECRET = "your-fixed-secret-key-123456";

    // 重载方法：添加 userType 参数，存入 Token 载荷
    public static String createToken(String data, String sign, String userType) {
        return JWT.create()
                .withAudience(data)
                .withClaim("userType", userType) // 添加用户类型载荷
                .withExpiresAt(DateUtil.offsetDay(new Date(), 1))
                // 使用固定密钥签名（替代 sign 参数，若需保留原有逻辑可自行调整）
                .sign(Algorithm.HMAC256(FIXED_SECRET));
    }

    // 保留原有方法（兼容旧代码）
    public static String createToken(String data, String sign) {
        return JWT.create()
                .withAudience(data)
                .withExpiresAt(DateUtil.offsetDay(new Date(), 1))
                .sign(Algorithm.HMAC256(sign));
    }
}