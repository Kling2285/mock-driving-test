package com.example.controller;

import com.example.common.Result;
import com.example.entity.Admin;
import com.example.entity.User;
import com.example.service.AdminService;
import com.example.service.EmailSendService;
import com.example.service.UserService;
import com.example.utils.PasswordEncoderUtil;
import com.example.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class LoginController {
    @Autowired
    private UserService userService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private EmailSendService emailSendService;

    /**
     * 重构后：密码登录（自动判断admin/user，不再接收loginType）
     * 逻辑：先查admin表 → 存在则管理员登录；不存在则查user表 → 存在则用户登录；都不存在则提示账号不存在
     */
    @PostMapping("/byPassword")
    public Result loginByPassword(@RequestBody Map<String, Object> map) {
        // 1. 基础参数校验（只校验用户名、密码，移除loginType）
        if (map.get("username") == null || map.get("password") == null) {
            return Result.error("用户名或密码不能为空");
        }
        String username = map.get("username").toString().trim();
        String rawPwd = map.get("password").toString().trim();

        if (username.isEmpty() || rawPwd.isEmpty()) {
            return Result.error("用户名或密码不能为空");
        }

        // 2. 第一步：查询管理员表
        List<Admin> adminList = adminService.findByUsername(username);
        if (!CollectionUtils.isEmpty(adminList)) {
            Admin loginAdmin = adminList.get(0);
            // 管理员账号存在，校验密码
            if (PasswordEncoderUtil.matchPassword(rawPwd, loginAdmin.getPassword())) {
                // 管理员登录成功，封装返回数据（userType固定为admin）
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("adminId", loginAdmin.getAdminId());
                resultMap.put("username", loginAdmin.getUsername());
                resultMap.put("realName", loginAdmin.getRealName());
                resultMap.put("roleId", loginAdmin.getRoleId());
                resultMap.put("email", loginAdmin.getEmail());
                resultMap.put("phone", loginAdmin.getPhone());
                resultMap.put("userType", "admin"); // 强制指定为admin，避免数据库值异常

                // ========== 修改点1：管理员 Token 生成，新增传入 "admin" 作为 userType ==========
                String token = TokenUtils.createToken(loginAdmin.getAdminId() + "", loginAdmin.getUsername(), "admin");
                resultMap.put("token", token);
                return Result.success(resultMap, "管理员登录成功");
            } else {
                // 管理员账号存在，但密码错误
                return Result.error("管理员密码错误");
            }
        }

        // 3. 第2步：查询用户表
        List<User> userList = userService.findByUsername(username);
        if (!CollectionUtils.isEmpty(userList)) {
            User loginUser = userList.get(0);
            // 直接调用密码比对工具类（和管理员逻辑一致，无需依赖login方法）
            boolean isPwdMatch = PasswordEncoderUtil.matchPassword(rawPwd, loginUser.getPassword());
            if (isPwdMatch) {
                // 用户登录成功，封装返回数据（userType固定为user）
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("userId", loginUser.getUserId());
                resultMap.put("username", loginUser.getUsername());
                resultMap.put("nickname", loginUser.getNickname());
                resultMap.put("email", loginUser.getEmail());
                resultMap.put("phone", loginUser.getPhone());
                resultMap.put("userType", "user"); // 强制指定为user，避免数据库值异常

                // ========== 修改点2：普通用户密码登录 Token 生成，新增传入 "user" 作为 userType ==========
                String token = TokenUtils.createToken(loginUser.getUserId() + "", loginUser.getUsername(), "user");
                resultMap.put("token", token);
                return Result.success(resultMap, "用户登录成功");
            } else {
                // 用户账号存在，但密码错误
                return Result.error("用户密码错误");
            }
        }

        // 4. 第三步：admin和user表都不存在该账号
        return Result.error("账号不存在");
    }

    /**
     * 邮箱登录（仅支持普通用户，保持原有逻辑，强制指定userType=user）
     */
    @PostMapping("/byEmail")
    public Result loginByEmail(@RequestBody Map<String, Object> map) {
        if (map.get("email") == null) {
            return Result.error("邮箱不能为空");
        }
        String email = map.get("email").toString().trim();
        if (email.isEmpty()) {
            return Result.error("邮箱不能为空");
        }

        List<User> userList = userService.findByEmail(email);
        User loginUser = CollectionUtils.isEmpty(userList) ? null : userList.get(0);

        if (loginUser == null) {
            return Result.error("该邮箱未注册，无法登录");
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("userId", loginUser.getUserId());
        resultMap.put("username", loginUser.getUsername());
        resultMap.put("nickname", loginUser.getNickname());
        resultMap.put("email", loginUser.getEmail());
        resultMap.put("phone", loginUser.getPhone());
        resultMap.put("userType", "user"); // 强制指定为user

        // ========== 修改点3：普通用户邮箱登录 Token 生成，新增传入 "user" 作为 userType ==========
        String token = TokenUtils.createToken(loginUser.getUserId() + "", loginUser.getUsername(), "user");
        resultMap.put("token", token);

        return Result.success(resultMap, "邮箱登录成功");
    }

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