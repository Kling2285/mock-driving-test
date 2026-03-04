package com.example.common;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.entity.User;
import com.example.entity.Admin;
import com.example.service.UserService;
import com.example.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Date;

@Component
public class JWTInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    private static final String FIXED_SECRET = "your-fixed-secret-key-123456";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 放过 OPTIONS 预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return true;
        }

        // 2. 获取 Token
        String token = request.getHeader("token");
        if (StrUtil.isBlank(token)) {
            token = request.getParameter("token");
        }

        // 3. 无 Token 直接抛出异常
        if (StrUtil.isBlank(token)) {
            throw new RuntimeException("无权操作，请先登录");
        }

        DecodedJWT decodedJWT = null;
        try {
            // 先解析 Token（不校验签名和过期，仅获取载荷信息）
            decodedJWT = JWT.decode(token);
        } catch (JWTDecodeException e) {
            throw new RuntimeException("Token 格式无效，请重新登录");
        }

        // 4. 校验 Token（优先推荐：通过载荷区分用户类型，避免互斥校验）
        // 假设登录时，Token 载荷中添加了 userType 字段（可在 TokenUtils 中修改）
        String userType = decodedJWT.getClaim("userType").asString();
        boolean isTokenValid = false;

        if ("user".equals(userType)) {
            isTokenValid = validateUserToken(token, decodedJWT);
        } else if ("admin".equals(userType)) {
            isTokenValid = validateAdminToken(token, decodedJWT);
        } else {
            throw new RuntimeException("Token 关联的用户类型未知");
        }

        // 5. 校验通过则放行
        return isTokenValid;
    }

    /**
     * 校验普通用户 Token（补充过期校验，替换签名密钥）
     */
    private boolean validateUserToken(String token, DecodedJWT decodedJWT) throws Exception {
        try {
            // 1. 解析用户 ID
            String audience = decodedJWT.getAudience().get(0);
            Long userId = Long.parseLong(audience);
            User user = userService.findById(userId);

            // 2. 校验用户是否存在
            if (user == null) {
                throw new RuntimeException("Token 关联的普通用户不存在");
            }

            // 3. 校验 Token 过期时间
            Date expiresAt = decodedJWT.getExpiresAt();
            if (expiresAt.before(new Date())) {
                throw new RuntimeException("Token 已过期，请重新登录");
            }

            // 4. 验证签名（使用固定密钥，或 user.getUserId().toString()（不可变））
            // 方案1：固定密钥（推荐）
            JWT.require(Algorithm.HMAC256(FIXED_SECRET)).build().verify(token);
            // 方案2：使用 userId（不可变，替代 username）
            // JWT.require(Algorithm.HMAC256(user.getUserId().toString())).build().verify(token);

            return true;
        } catch (JWTVerificationException e) {
            throw new RuntimeException("用户 Token 无效或已过期");
        }
    }

    /**
     * 校验管理员 Token（补充过期校验，替换签名密钥）
     */
    private boolean validateAdminToken(String token, DecodedJWT decodedJWT) throws Exception {
        try {
            // 1. 解析管理员 ID
            String audience = decodedJWT.getAudience().get(0);
            Long adminId = Long.parseLong(audience);
            Admin admin = adminService.findById(adminId);

            // 2. 校验管理员是否存在
            if (admin == null) {
                throw new RuntimeException("Token 关联的管理员不存在");
            }

            // 3. 校验 Token 过期时间
            Date expiresAt = decodedJWT.getExpiresAt();
            if (expiresAt.before(new Date())) {
                throw new RuntimeException("Token 已过期，请重新登录");
            }

            // 4. 验证签名（使用固定密钥，或 admin.getAdminId().toString()（不可变））
            JWT.require(Algorithm.HMAC256(FIXED_SECRET)).build().verify(token);
            // JWT.require(Algorithm.HMAC256(admin.getAdminId().toString())).build().verify(token);

            return true;
        } catch (JWTVerificationException e) {
            throw new RuntimeException("管理员 Token 无效或已过期");
        }
    }
}