import request from "@/utils/request.js";

// 原有：用户名+密码登录
export function loginByPassword(data) {
    return request.post("/login/byPassword", data); // JSON格式传参（和你原有逻辑一致）
}

// 新增：邮箱+验证码登录
export function loginByEmail(data) {
    return request.post("/login/byEmail", data); // JSON格式传参（无需FormData）
}

// 补充：校验邮箱是否存在（对应后端 /login/checkExist 接口，GET请求+URL参数）
export function checkEmailExist(email) {
    return request.get("/login/checkExist", {
        params: {
            email: email // 对应后端@RequestParam(name = "email")的参数名
        }
    });
}

// 补充：发送邮箱验证码（对应后端 /login/sendCode 接口，GET请求+URL参数）
export function sendVerifyCode(email) {
    return request.get("/login/sendCode", {
        params: {
            email: email // 对应后端@RequestParam(name = "email")的参数名
        }
    });
}

// 补充：验证邮箱验证码（对应后端 /login/checkCode 接口，POST请求+URL参数）
export function checkVerifyCode(params) {
    return request.post("/login/checkCode", null, {
        params: {
            email: params.email, // 对应后端邮箱参数
            code: params.code     // 对应后端验证码参数
        }
    });
}