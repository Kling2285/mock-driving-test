import request from "@/utils/request.js";

// 发送注册邮箱验证码
export function sendRegisterEmailCode(data) {
    return request.post("/register/sendEmailCode", data);
}

// 邮箱注册提交
export function emailRegister(data) {
    return request.post("/register/byEmail", data);
}