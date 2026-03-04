import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    timeout: 5000,
    withCredentials: true,
})

// 请求拦截器（优化：优先使用单独存储的 token，兜底使用 login_user 解析）
request.interceptors.request.use(
    config => {
        // 1. 优先获取单独存储的 token（最稳定）
        let token = localStorage.getItem('token');
        // 2. 若未单独存储，再从 login_user 中解析（兜底逻辑）
        if (!token) {
            let user = localStorage.getItem('login_user');
            if (user) {
                try {
                    user = JSON.parse(user);
                    token = user.token || '';
                } catch (e) {
                    console.error('解析login_user失败：', e);
                    localStorage.removeItem('login_user');
                    localStorage.removeItem('token'); // 同步删除无效 token
                }
            }
        }
        // 3. 携带 Token 到请求头（和后端 JWT 拦截器对应，字段名：token）
        if (token) {
            config.headers.token = token;
        } else {
            console.log('无有效 Token，请先登录');
        }
        return config;
    },
    error => {
        return Promise.reject(error);
    }
);

// 响应拦截器（修复：统一使用 localStorage，移除 sessionStorage 操作）
request.interceptors.response.use(
    response => {
        if (response.config.responseType === 'blob') {
            return response;
        }
        const res = response.data
        if (res.code !== 200) {
            ElMessage.error(res.msg || '操作失败，请重试')
            return Promise.reject(res.msg || '请求异常')
        }
        return res
    },
    error => {
        if (error.response) {
            const { status } = error.response
            if (error.config?.responseType === 'blob') {
                ElMessage.error('文件导出/下载失败')
                return Promise.reject(error)
            }
            switch (status) {
                case 401:
                    ElMessage.error('登录已过期，请重新登录')
                    // 修复：只操作 localStorage（和登录逻辑一致），移除 sessionStorage
                    localStorage.removeItem('token');
                    localStorage.removeItem('login_user');
                    router.push('/login')
                    break
                case 403:
                    ElMessage.error('没有权限访问，请联系管理员')
                    break
                case 404:
                    ElMessage.error('请求的接口不存在')
                    break
                case 500:
                    ElMessage.error('服务器内部错误，请稍后再试')
                    break
                default:
                    ElMessage.error(`请求失败（${status}）：${error.message}`)
            }
        } else {
            ElMessage.error('网络连接异常，请检查网络')
        }
        return Promise.reject(error)
    }
);

// 下载方法（无需修改，已兼容 Token 传递）
export function download(url, params = {}) {
    try {
        const base_url = import.meta.env.VITE_API_BASE_URL;
        const searchParam = new URLSearchParams();
        for (let key in params) {
            if (params[key] !== '' && params[key] !== undefined && params[key] !== null) {
                searchParam.append(key, params[key]);
            }
        }
        let token = localStorage.getItem('token');
        // 优先使用单独存储的 token
        if (!token) {
            let user = localStorage.getItem('login_user');
            if (user) {
                user = JSON.parse(user);
                token = user.token || '';
            }
        }
        if (token) {
            searchParam.append('token', token);
        }
        const fullUrl = `${base_url}${url}?${searchParam.toString()}`;
        window.location.href = fullUrl;
    } catch (e) {
        console.error('下载方法异常：', e);
        ElMessage.error('下载参数解析失败');
    }
}

export default request