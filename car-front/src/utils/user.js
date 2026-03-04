
import { defineStore } from 'pinia'

// 定义并导出 Store（唯一标识：user）
export const useUserStore = defineStore('user', {
    state: () => ({
        token: sessionStorage.getItem('token') || '', // 从本地缓存初始化
        loginUser: JSON.parse(sessionStorage.getItem('login_user')) || null // 登录用户信息
    }),

    // 2. 操作：修改状态的方法（同步/异步都支持）
    actions: {
        // 登录：存储 token 和用户信息
        login(token, userInfo) {
            this.token = token
            this.loginUser = userInfo
            // 同步到 sessionStorage 持久化
            sessionStorage.setItem('token', token)
            sessionStorage.setItem('login_user', JSON.stringify(userInfo))
        },

        // 退出：清空状态
        logout() {
            this.token = ''
            this.loginUser = null
            // 清空本地缓存
            sessionStorage.removeItem('token')
            sessionStorage.removeItem('login_user')
        },

        // 刷新用户信息（比如修改个人资料后）
        updateUserInfo(newInfo) {
            this.loginUser = { ...this.loginUser, ...newInfo }
            sessionStorage.setItem('login_user', JSON.stringify(this.loginUser))
        }
    },

    // 3. 计算属性：派生状态（可选）
    getters: {
        // 判断是否登录（简化组件内的判断逻辑）
        isLogin: (state) => !!state.token,
        // 获取用户名（快捷访问）
        username: (state) => state.loginUser?.username || '未登录'
    }
})