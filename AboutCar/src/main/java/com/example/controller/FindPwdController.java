package com.example.controller;

import com.example.common.Result;
import com.example.entity.User;
import com.example.service.UserService;
import com.example.service.EmailSendService;

import com.example.utils.PasswordEncoderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.time.LocalDateTime;


@RestController
@RequestMapping("/findPwd")
public class FindPwdController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailSendService emailSendService;

    /**
     * 1. 发送找回密码的邮箱验证码
     * 请求URL：/findPwd/sendEmailCode
     * 请求参数：{ "email": "xxx@xxx.com" }
     */
    @PostMapping("/sendEmailCode")
    public Result sendFindPwdEmailCode(@RequestBody Map<String, Object> map) {
        if (map.get("email") == null || map.get("email").toString().trim().isEmpty()) {
            return Result.error("邮箱不能为空");
        }
        String email = map.get("email").toString().trim();

        if (!userService.existsByEmail(email)) {
            return Result.error("该邮箱未注册，无法找回密码");
        }

        try {
            emailSendService.sendVerifyCodeEmail(email);
            return Result.success("验证码已发送至您的邮箱，有效期10分钟");
        } catch (Exception e) {
            return Result.error("验证码发送失败：" + e.getMessage());
        }
    }

    /**
     * 2. 验证找回密码的邮箱验证码
     * 请求URL：/findPwd/checkEmailCode
     * 请求参数：{ "email": "xxx@xxx.com", "code": "123456" }
     */
    @PostMapping("/checkEmailCode")
    public Result checkEmailCode(@RequestBody Map<String, Object> map) {
        if (map.get("email") == null || map.get("email").toString().trim().isEmpty()) {
            return Result.error("邮箱不能为空");
        }
        if (map.get("code") == null || map.get("code").toString().trim().isEmpty()) {
            return Result.error("验证码不能为空");
        }
        String email = map.get("email").toString().trim();
        String code = map.get("code").toString().trim();

        try {
            boolean isValid = emailSendService.checkVerifyCode(email, code);
            if (isValid) {
                return Result.success("验证码验证通过");
            } else {
                return Result.error("验证码无效/过期");
            }
        } catch (Exception e) {
            return Result.error("验证码验证失败：" + e.getMessage());
        }
    }

    /**
     * 3. 重置密码（已加密，不再明文存储123456）
     * 请求URL：/findPwd/resetByEmail
     * 请求参数：{ "email": "xxx@xxx.com" }
     */
    @PostMapping("/resetByEmail")
    public Result resetPwdByEmail(@RequestBody Map<String, Object> map) {
        if (map.get("email") == null || map.get("email").toString().trim().isEmpty()) {
            return Result.error("邮箱不能为空");
        }
        String email = map.get("email").toString().trim();
        // 默认新密码（明文）
        String newPwd = "123456";

        if (!userService.existsByEmail(email)) {
            return Result.error("该邮箱未注册，无法重置密码");
        }

        User user = null;
        try {
            user = userService.findByEmail(email).get(0);
        } catch (Exception e) {
            return Result.error("未查询到该邮箱对应的用户");
        }

        // ========== 核心修改：加密默认新密码，不再明文存储 ==========
        String encodedNewPwd = PasswordEncoderUtil.encodePassword(newPwd);
        user.setPassword(encodedNewPwd); // 设置加密后的新密码
        user.setUpdateTime(LocalDateTime.now());

        try {
            boolean updateSuccess = userService.update(user);
            if (updateSuccess) {
                return Result.success("密码重置成功！新密码为123456，请及时修改");
            } else {
                return Result.error("密码重置失败，请稍后重试");
            }
        } catch (Exception e) {
            return Result.error("密码重置异常：" + e.getMessage());
        }
    }
}