import request from "@/utils/request.js";

// 发送找回密码的邮箱验证码
export function sendFindPwdEmailCode(data) {
    return request.post("/findPwd/sendEmailCode", data);
}

// 验证邮箱验证码是否有效
export function checkEmailCode(data) {
    return request.post("/findPwd/checkEmailCode", data);
}

// 根据邮箱重置密码
export function resetPwdByEmail(data) {
    return request.post("/findPwd/resetByEmail", data);
}