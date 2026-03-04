package com.example.entity;


import lombok.Data;
import java.time.LocalDateTime;

@Data
public class VerifyCode {
    private Long id;          // 主键自增ID
    private String email;     // 收件人邮箱
    private String code;      // 6位验证码
    private LocalDateTime expireTime; // 过期时间（1分钟）
    private LocalDateTime createTime = LocalDateTime.now(); // 创建时间（默认当前时间）
    private Integer used = 0; // 是否使用（0=未用，1=已用）
}