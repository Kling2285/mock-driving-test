import request from "@/utils/request.js";

// 发送验证码（GET请求，参数拼接到URL）
export function sendVerifyCode(email) {
    return request.get("/email/sendCode", {
        params: { email }
    });
}

//验证邮箱是否存在
export function checkEmailExist(email) {
    return request.get("/email/checkExist", {
        params: { email }
    });
}

// 验证验证码（最终修复版：强制覆盖全局请求头）
export function checkVerifyCode(data) {
    if (!data.email || !data.code) {
        return Promise.reject(new Error("邮箱或验证码不能为空"));
    }

    const formData = new URLSearchParams();
    formData.append("email", data.email);
    formData.append("code", data.code);

    return request({
        url: "/email/checkCode",
        method: "POST",
        data: formData,
        // 关键：覆盖全局的 Content-Type，且合并 token 头
        headers: {
            ...request.defaults.headers, // 继承全局头（如 token）
            "Content-Type": "application/x-www-form-urlencoded" // 覆盖 JSON 头
        }
    });
}