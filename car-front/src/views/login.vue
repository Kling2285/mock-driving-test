<template>
  <div class="main">
    <div class="login-box">
      <div class="login-left">
        <div class="login-title">驾校模拟考试系统</div>
        <div class="login-desc">账号密码登录</div>
        <div class="login-footer">© 2025 安全管理平台</div>
      </div>
      <div class="login-right">
        <h1 class="login-title-right" style="margin-bottom: 20px">登录</h1>

        <div class="login-type-group" style="margin-bottom: 30px;">
          <el-button
              class="login-type-btn"
              :class="{ active: activeType === 'password' }"
              @click="activeType = 'password'"
          >
            密码登录
          </el-button>
          <el-button
              class="login-type-btn"
              :class="{ active: activeType === 'phone' }"
              @click="activeType = 'phone'"
          >
            电话号码登录
          </el-button>
          <el-button
              class="login-type-btn"
              :class="{ active: activeType === 'email' }"
              @click="activeType = 'email'"
          >
            邮箱登录
          </el-button>
        </div>

        <!-- 密码登录表单：保留原有逻辑，不修改 -->
        <el-form
            v-if="activeType === 'password'"
            :model="form"
            :rules="rules"
            label-width="80px"
            style="max-width: 600px"
            ref="loginForm"
            class="login-form"
        >
          <el-form-item label="用户名" prop="username">
            <el-input
                v-model="form.username"
                autofocus
                placeholder="请输入用户名"
                :prefix-icon="User"
                class="login-input"
            />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input
                v-model="form.password"
                type="password"
                @keyup.enter="handleLogin"
                show-password
                placeholder="请输入密码"
                :prefix-icon="Lock"
                class="login-input"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="login-btn" @click="handleLogin">登录</el-button>
          </el-form-item>
        </el-form>

        <!-- 手机号登录表单：保留原有结构，不修改 -->
        <el-form
            v-if="activeType === 'phone'"
            label-width="80px"
            style="max-width: 600px"
            class="login-form"
        >
          <el-form-item label="电话号码" prop="phone">
            <el-input
                v-model="phoneForm.phone"
                placeholder="请输入电话号码"
                :prefix-icon="Phone"
                class="login-input"
            />
          </el-form-item>
          <el-form-item label="验证码" prop="code" class="code-item">
            <el-input
                v-model="phoneForm.code"
                placeholder="请输入验证码"
                class="login-input code-input"
            />
            <el-button class="code-btn">获取验证码</el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="login-btn" @click="handlePhoneLogin">登录</el-button>
          </el-form-item>
        </el-form>

        <!-- 邮箱登录表单：仅保留v-show修改（原有修复），其他结构不变 -->
        <el-form
            v-show="activeType === 'email'"
            :model="emailForm"
            :rules="emailRules"
            label-width="80px"
            style="max-width: 600px"
            class="login-form"
            ref="emailLoginForm"
        >
          <el-form-item label="邮箱" prop="email">
            <el-input
                v-model="emailForm.email"
                placeholder="请输入邮箱"
                :prefix-icon="Message"
                class="login-input"
            />
          </el-form-item>
          <el-form-item label="验证码" prop="code" class="code-item">
            <el-input
                v-model="emailForm.code"
                placeholder="请输入6位数字验证码"
                class="login-input code-input"
            />
            <el-button
                class="code-btn"
                @click="getEmailCode"
                :disabled="codeBtnDisabled"
            >
              {{ codeBtnText }}
            </el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="login-btn" @click="handleEmailLogin">登录</el-button>
          </el-form-item>
        </el-form>

        <div class="register-link">
          没有账号，<router-link to="/register">去注册</router-link>
        </div>
        <div class="register-link">
          忘记密码，<router-link to="/getback">找回密码</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onUnmounted } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from 'vue-router';
import { loginByPassword, loginByEmail } from "@/api/login.js";
import { sendVerifyCode, checkVerifyCode, checkEmailExist } from "@/api/code.js";

import { Lock, User, Phone, Message } from '@element-plus/icons-vue'

const router = useRouter();
const activeType = ref('password');

// 密码登录表单数据：不修改
const form = reactive({
  username: '',
  password: ''
});
// 手机号登录表单数据：不修改
const phoneForm = reactive({
  phone: '',
  code: ''
});
// 邮箱登录表单数据：不修改
const emailForm = reactive({
  email: '',
  code: ''
});

// 表单ref：不修改
const loginForm = ref();
const emailLoginForm = ref();

// 密码登录校验规则：不修改
const rules = reactive({
  username: [
    { required: true, message: '请填写用户名', trigger: 'change' }
  ],
  password: [
    { required: true, message: '请填写密码', trigger: 'change' }
  ]
});

// 邮箱登录校验规则：不修改
const emailRules = reactive({
  email: [
    { required: true, message: '请填写邮箱', trigger: 'change' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'change' }
  ],
  code: [
    { required: true, message: '请填写验证码', trigger: 'change' },
    { pattern: /^\d{6}$/, message: '请输入6位数字验证码', trigger: 'change' }
  ]
});

// 验证码按钮状态：核心优化，防止重复提交
const codeBtnDisabled = ref(false);
const codeBtnText = ref('获取验证码');
let countdownTimer = null; // 倒计时定时器


// 发送邮箱验证码：移除格式校验，仅做空值判断，直接校验邮箱存在性
const getEmailCode = () => {
  // 1. 先校验邮箱是否为空（仅保留空值判断，移除格式校验）
  if (!emailForm.email) {
    ElMessage.warning("请先输入邮箱");
    return;
  }

  // 2. 关键：点击后立即禁用按钮，防止重复提交
  codeBtnDisabled.value = true;

  // 3. 直接调用接口校验邮箱是否存在
  checkEmailExist(emailForm.email)
      .then(existRes => {
        // 邮箱不存在：提示错误，并恢复按钮可用状态
        if (existRes.code !== 200) {
          ElMessage.error(existRes.msg || "该邮箱未注册，无法发送验证码");
          codeBtnDisabled.value = false; // 恢复按钮
          codeBtnText.value = '获取验证码'; // 恢复文本
          return;
        }

        // 邮箱存在：发送验证码
        sendVerifyCode(emailForm.email)
            .then(res => {
              if (res.code === 200) {
                ElMessage.success("验证码已发送，请注意查收");
                // 启动倒计时（60秒后解禁按钮）
                let countdown = 60;
                codeBtnText.value = `重新发送(${countdown}s)`;

                countdownTimer = setInterval(() => {
                  countdown--;
                  codeBtnText.value = `重新发送(${countdown}s)`;
                  if (countdown <= 0) {
                    clearInterval(countdownTimer);
                    codeBtnDisabled.value = false; // 倒计时结束，解禁按钮
                    codeBtnText.value = '获取验证码'; // 恢复文本
                    countdownTimer = null; // 重置定时器
                  }
                }, 1000);
              } else {
                ElMessage.error("发送失败：" + res.msg);
                codeBtnDisabled.value = false; // 发送失败，恢复按钮
                codeBtnText.value = '获取验证码'; // 恢复文本
              }
            })
            .catch(err => {
              console.error('发送验证码失败：', err);
              ElMessage.error("发送验证码请求失败，请稍后重试");
              codeBtnDisabled.value = false; // 请求异常，恢复按钮
              codeBtnText.value = '获取验证码'; // 恢复文本
            });
      })
      .catch(err => {
        console.error('校验邮箱失败：', err);
        ElMessage.error("校验邮箱失败，请稍后重试");
        codeBtnDisabled.value = false; // 校验异常，恢复按钮
        codeBtnText.value = '获取验证码'; // 恢复文本
      });
};

// 邮箱+验证码登录：保留原有逻辑，确保验证码比对成功后执行登录
const handleEmailLogin = () => {
  emailLoginForm.value?.validate((isValid) => {
    if (!isValid) return;

    // 1. 先校验验证码是否有效
    checkVerifyCode({
      email: emailForm.email,
      code: emailForm.code
    })
        .then((checkRes) => {
          // 2. 验证码比对成功（核心：仅此时执行登录逻辑）
          if (checkRes.code === 200) {
            loginByEmail({
              email: emailForm.email
            })
                .then((loginRes) => {
                  if (loginRes.code === 200) {
                    const userInfo = loginRes.data;
                    localStorage.setItem("login_user", JSON.stringify(userInfo));
                    localStorage.setItem("token", userInfo.token);
                    ElMessage.success("登录成功");
                    emailForm.email = '';
                    emailForm.code = '';
                    redirectByUserType(userInfo.userType);
                  } else {
                    ElMessage.error("登录失败：" + loginRes.msg);
                  }
                })
                .catch(() => {
                  ElMessage.error("登录请求失败，请稍后重试");
                });
          } else {
            // 验证码无效/过期：提示错误，不执行登录
            ElMessage.error("验证码无效/过期：" + checkRes.msg);
          }
        })
        .catch(() => {
          ElMessage.error("验证验证码请求失败，请稍后重试");
        });
  });
};

// 密码登录逻辑：不修改
const handleLogin = () => {
  loginForm.value?.validate((isValid) => {
    if (!isValid) return;

    loginByPassword({
      username: form.username,
      password: String(form.password)
    })
        .then((loginRes) => {
          if (loginRes.code === 200) {
            const userInfo = loginRes.data;
            localStorage.setItem("login_user", JSON.stringify(userInfo));
            localStorage.setItem("token", userInfo.token);
            ElMessage.success(loginRes.msg);
            redirectByUserType(userInfo.userType);
            form.username = '';
            form.password = '';
          } else {
            ElMessage.error("登录失败：" + loginRes.msg);
          }
        })
        .catch((err) => {
          console.error("登录请求异常：", err);
          ElMessage.error("登录请求失败，请稍后重试");
        });
  });
};

// 跳转逻辑：不修改
const redirectByUserType = (userType) => {
  if (userType === 'admin') {
    router.push("/amanager/user");
  } else if (userType === 'user') {
    router.push("/umanager/uhome");
  } else {
    router.push("/");
    ElMessage.warning("未知用户类型，默认跳转到首页");
  }
};

// 手机号登录逻辑：不修改
const handlePhoneLogin = () => {
  ElMessage.info("手机号登录功能暂未实现，请使用其他登录方式");
};

// 组件卸载时清除定时器：不修改（增强稳定性）
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer);
    countdownTimer = null;
  }
});
</script>

<style scoped>
.main {
  width: 100%;
  height: 100vh;
  background-color: #e0efff;
  background-size: cover;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-box{
  width: 700px;
  height: 480px;
  background-color: white;
  border-radius: 10px;
  padding: 0;
  box-shadow: 0 4px 20px rgba(0,0,0,0.12);
  display: flex;
  overflow: hidden;
}

.login-left{
  width: 40%;
  height: 100%;
  background: linear-gradient(135deg, #88C6ED, #0058fb);
  color: white;
  padding: 40px 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  box-sizing: border-box;
}
.login-title{
  font-size: 26px;
  font-weight: 700;
  margin-bottom: 10px;
}
.login-desc{
  font-size: 16px;
  opacity: 0.9;
}
.login-footer{
  font-size: 12px;
  opacity: 0.8;
}

.login-right{
  width: 60%;
  height: 100%;
  padding: 30px 40px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-title-right {
  text-align: center;
  font-size: 22px;
  font-weight: 600;
  color: #333;
}

.login-type-group {
  display: flex;
  gap: 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

:deep(.login-type-btn) {
  flex: 1;
  height: 40px;
  border: none !important;
  border-radius: 0 !important;
  background-color: transparent !important;
  color: #333 !important;
  font-size: 14px;
  transition: all 0.2s ease;
}

:deep(.login-type-btn.active) {
  background-color: #1E90FF !important;
  color: #fff !important;
}

:deep(.login-type-btn:not(.active):hover) {
  background-color: #f8f8f8 !important;
}

.login-form {
  width: 100%;
}

.login-input {
  height: 40px;
  width: 100%;
  border-radius: 4px;
}

.code-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.code-input {
  flex: 1;
}

.code-btn {
  width: 120px;
  height: 40px;
  background-color: #0d9ef4 !important;
  border-color: #0d9ef4 !important;
  color: #fff !important;
  white-space: nowrap;
}

.login-btn {
  width: 100%;
  height: 45px;
  font-size: 18px;
  background-color: #1E90FF !important;
  border-color: #1E90FF  !important;
}

.register-link {
  text-align: right;
  margin-top: 10px;
  font-size: 14px;
  color: #666;
}

.register-link a {
  color: #6E9BC5;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>