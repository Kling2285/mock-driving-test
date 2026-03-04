package com.example.controller;


import com.example.common.Result;
import com.example.service.EmailSendService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private EmailSendService emailSendService;

    @Autowired
    private UserService userService;

    /**
     * 校验邮箱是否存在
     * @param email 待校验的邮箱
     * @return Result 统一返回格式
     */
    @GetMapping("/checkExist")
    public Result checkEmailExist(@RequestParam(name = "email") String email) {
        boolean exists = userService.existsByEmail(email);
        if (exists) {
            return Result.success(null, "邮箱存在");
        } else {
            return Result.error("该邮箱未注册，无法发送验证码", 500);
        }
    }

    /**
     * 发送邮箱验证码
     * @param email 接收验证码的邮箱
     * @return Result 统一返回格式
     */
    @GetMapping("/sendCode")
    public Result sendCode(@RequestParam(name = "email") String email) {
        try {
            boolean exists = userService.existsByEmail(email);
            if (!exists) {
                return Result.error("该邮箱未注册，无法发送验证码", 500);
            }
            emailSendService.sendVerifyCodeEmail(email);
            return Result.success(null, "验证码发送成功");
        } catch (Exception e) {
            return Result.error("验证码发送失败：" + e.getMessage(), 500);
        }
    }

    /**
     * 验证邮箱验证码
     * @param email 邮箱
     * @param code 验证码
     * @return Result 统一返回格式
     */
    @PostMapping("/checkCode")
    public Result checkCode(
            @RequestParam(name = "email") String email,
            @RequestParam(name = "code") String code
    ) {
        try {
            boolean result = emailSendService.checkVerifyCode(email, code);
            if (result) {
                return Result.success(null, "验证通过");
            } else {
                return Result.error("验证码无效/过期", 500);
            }
        } catch (Exception e) {
            return Result.error("验证码验证失败：" + e.getMessage(), 500);
        }
    }
}