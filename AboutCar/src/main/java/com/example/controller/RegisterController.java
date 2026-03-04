package com.example.controller;

import com.example.common.Result;
import com.example.entity.User;
import com.example.service.EmailSendService;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/register")
public class RegisterController {
    @Autowired
    private UserService userService;

    @Autowired
    private EmailSendService emailSendService;

    /**
     * 邮箱注册（仅支持普通用户，移除密码加密，适配现有SSM代码）
     */
    @PostMapping("/byEmail")
    @Transactional
    public Result registerByEmail(@RequestBody Map<String, Object> map) {
        // 1. 提取注册参数并做非空校验（安全提取，避免空指针）
        String username = null;
        String rawPassword = null;
        String nickname = null;
        String phone = null;
        String email = null;
        String code = null;

        if (map.get("username") != null) {
            username = map.get("username").toString().trim();
        }
        if (map.get("password") != null) {
            rawPassword = map.get("password").toString().trim();
        }
        if (map.get("nickname") != null) {
            nickname = map.get("nickname").toString().trim();
        }
        if (map.get("phone") != null) {
            phone = map.get("phone").toString().trim();
        }
        if (map.get("email") != null) {
            email = map.get("email").toString().trim();
        }
        if (map.get("code") != null) {
            code = map.get("code").toString().trim();
        }

        // 校验所有参数不能为空
        if (!StringUtils.hasText(username) || !StringUtils.hasText(rawPassword)
                || !StringUtils.hasText(nickname) || !StringUtils.hasText(phone)
                || !StringUtils.hasText(email) || !StringUtils.hasText(code)) {
            return Result.error("所有注册参数不能为空");
        }

        // 2. 校验邮箱是否已注册（复用你已实现的existsByEmail方法）
        boolean emailExist = userService.existsByEmail(email);
        if (emailExist) {
            return Result.error("该邮箱已注册，无法重复注册");
        }

        // 3. 校验用户名是否已注册（复用你已实现的findByCondition方法）
        List<User> usernameUserList = userService.findByCondition(null, username, null);
        if (!CollectionUtils.isEmpty(usernameUserList)) {
            return Result.error("该用户名已注册，请更换用户名");
        }

        // 4. 校验手机号是否已注册（遍历条件查询结果实现，适配你现有方法）
        List<User> allUserList = userService.findByCondition(null, null, null);
        boolean phoneExist = false;
        if (!CollectionUtils.isEmpty(allUserList)) {
            for (User user : allUserList) {
                if (phone.equals(user.getPhone())) {
                    phoneExist = true;
                    break;
                }
            }
        }
        if (phoneExist) {
            return Result.error("该手机号已注册");
        }

        // 5. 校验验证码有效性
        boolean codeValid = emailSendService.checkVerifyCode(email, code);
        if (!codeValid) {
            return Result.error("验证码无效/过期，请重新获取");
        }

        // 6. 构建User实体对象（直接存储明文密码，无加密，完全适配你的实体）
        User user = new User();
        user.setUsername(username);
        user.setPassword(rawPassword); // 直接存储明文密码，未加密
        user.setNickname(nickname);
        user.setPhone(phone);
        user.setEmail(email);
        user.setAvatar(""); // 默认头像为空，可自定义
        user.setUserType("user"); // 固定为普通用户类型，与登录逻辑一致

        // 7. 保存用户（调用你已实现的insert方法，包含业务校验）
        try {
            boolean saveSuccess = userService.insert(user);
            if (saveSuccess) {
                return Result.success(user, "注册成功，请前往登录");
            } else {
                return Result.error("注册失败，系统异常");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("注册失败：" + e.getMessage());
        }
    }

    /**
     * 发送注册验证码（仅支持普通用户，适配现有逻辑）
     */
    @PostMapping("/sendEmailCode")
    public Result sendRegisterEmailCode(@RequestBody Map<String, Object> map) {
        // 提取并校验邮箱参数
        String email = null;
        if (map.get("email") != null) {
            email = map.get("email").toString().trim();
        }

        if (!StringUtils.hasText(email)) {
            return Result.error("邮箱不能为空");
        }

        // 校验邮箱是否已注册
        boolean emailExist = userService.existsByEmail(email);
        if (emailExist) {
            return Result.error("该邮箱已注册，无需发送验证码");
        }

        // 发送验证码
        try {
            emailSendService.sendVerifyCodeEmail(email);
            return Result.success("验证码已发送至您的邮箱，有效期10分钟");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("验证码发送失败：" + e.getMessage());
        }
    }
}