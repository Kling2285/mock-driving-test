<template>
  <div class="main">
    <div class="login-box">
      <div class="login-left">
        <div class="login-title">驾校模拟考试系统</div>
        <div class="login-desc">密码找回</div>
        <div class="login-footer">© 2025 安全管理平台</div>
      </div>
      <div class="login-right">
        <h1 class="login-title-right" style="margin-bottom: 20px">密码找回</h1>

        <div class="login-type-group" style="margin-bottom: 30px;">
          <el-button
              class="login-type-btn"
              :class="{ active: activeType === 'phone' }"
              @click="activeType = 'phone'"
          >
            电话找回
          </el-button>
          <el-button
              class="login-type-btn"
              :class="{ active: activeType === 'email' }"
              @click="activeType = 'email'"
          >
            邮箱找回
          </el-button>
        </div>

        <!-- 电话找回表单（保留结构，暂不实现逻辑） -->
        <el-form
            v-if="activeType === 'phone'"
            :model="phoneForm"
            :rules="phoneRules"
            label-width="auto"
            style="max-width: 600px"
            ref="phoneFormRef"
            class="login-form"
        >
          <el-form-item label="电话" prop="phone" style="margin-bottom: 25px;">
            <el-input v-model="phoneForm.phone" placeholder="请输入电话号码" class="login-input"/>
          </el-form-item>
          <el-form-item label="验证码" prop="code" style="margin-bottom: 25px;" class="code-item">
            <el-input
                v-model="phoneForm.code"
                placeholder="请输入短信验证码"
                class="login-input code-input"
            />
            <el-button class="code-btn">获取验证码</el-button>
          </el-form-item>
          <el-form-item>
            <el-button
                type="primary"
                class="login-btn"
                @click="handlePhoneFindPwd"
            >
              找回密码
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 邮箱找回密码表单（核心功能） -->
        <el-form
            v-if="activeType === 'email'"
            :model="emailForm"
            :rules="emailRules"
            label-width="auto"
            style="max-width: 600px"
            ref="emailFormRef"
            class="login-form"
        >
          <el-form-item label="邮箱" prop="email" style="margin-bottom: 25px;">
            <el-input v-model="emailForm.email" placeholder="请输入邮箱" class="login-input"/>
          </el-form-item>
          <el-form-item label="验证码" prop="code" style="margin-bottom: 25px;" class="code-item">
            <el-input
                v-model="emailForm.code"
                placeholder="请输入邮箱验证码"
                class="login-input code-input"
            />
            <el-button
                class="code-btn"
                :disabled="codeBtnDisabled"
                @click="getEmailCode"
            >
              {{ codeBtnText }}
            </el-button>
          </el-form-item>
          <el-form-item>
            <el-button
                type="primary"
                class="login-btn"
                @click="handleEmailFindPwd"
                :loading="resetLoading"
            >
              重置密码
            </el-button>
          </el-form-item>
        </el-form>

        <div class="register-link">
          已有账号，<router-link to="/login">去登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onUnmounted } from "vue";
import { ElMessage } from "element-plus";
import { useRouter } from 'vue-router';
// 导入密码找回接口（替换原有注册接口）
import { sendFindPwdEmailCode, checkEmailCode, resetPwdByEmail } from "@/api/findPwd.js"

const router = useRouter();
// 切换状态
const activeType = ref('phone');

// 电话找回表单（精简字段）
const phoneForm = reactive({
  phone: '',
  code: ''
});
const phoneFormRef = ref();

// 邮箱找回表单（精简字段，仅保留邮箱和验证码）
const emailForm = reactive({
  email: '',
  code: ''
});
const emailFormRef = ref();

// 邮箱找回相关状态
const codeBtnDisabled = ref(false);
const codeBtnText = ref('获取验证码');
const resetLoading = ref(false); // 重置密码加载状态
let countdownTimer = null;

// 电话找回校验规则
const phoneRules = reactive({
  phone: [
    { required: true, message: '请填写手机号', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback();
          return;
        }
        if (!/^1[3-9]\d{9}$/.test(value)) {
          callback(new Error('请输入正确的手机号'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  code: [
    { required: true, message: '请填写短信验证码', trigger: 'blur' }
  ]
});

// 邮箱找回校验规则（增强验证码长度校验）
const emailRules = reactive({
  email: [
    { required: true, message: '请填写邮箱', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (!value) {
          callback();
          return;
        }
        if (!/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(value)) {
          callback(new Error('请输入正确的邮箱'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  code: [
    { required: true, message: '请填写邮箱验证码', trigger: 'blur' },
    { len: 6, message: '验证码必须为6位数字', trigger: 'blur' }
  ]
});

// 电话找回密码
const handlePhoneFindPwd = () => {
  ElMessage.warning("电话找回功能暂未开通");
};

// 获取邮箱找回密码的验证码
const getEmailCode = () => {
  // 先校验邮箱格式
  emailFormRef.value.validateField('email', (error) => {

    // 调用发送验证码接口
    sendFindPwdEmailCode({ email: emailForm.email })
        .then(res => {
          if (res.code === 200) {
            ElMessage.success(res.msg);
            // 启动倒计时
            codeBtnDisabled.value = true;
            let countdown = 60;
            codeBtnText.value = `重新发送(${countdown}s)`;

            countdownTimer = setInterval(() => {
              countdown--;
              codeBtnText.value = `重新发送(${countdown}s)`;
              if (countdown <= 0) {
                clearInterval(countdownTimer);
                codeBtnDisabled.value = false;
                codeBtnText.value = '获取验证码';
              }
            }, 1000);
          } else {
            ElMessage.error(res.msg);
          }
        })
        .catch(() => {
          ElMessage.error("发送验证码失败，请稍后重试");
        });
  });
};

// 邮箱找回密码核心逻辑（验证验证码+重置密码）
const handleEmailFindPwd = () => {
  // 先校验表单完整性
  emailFormRef.value.validate((valid) => {
    if (!valid) return;

    resetLoading.value = true;
    // 1. 先验证验证码是否有效
    checkEmailCode({ email: emailForm.email, code: emailForm.code })
        .then(res => {
          if (res.code !== 200) {
            ElMessage.error(res.msg);
            resetLoading.value = false;
            return;
          }

          // 2. 验证码有效，调用重置密码接口
          return resetPwdByEmail({ email: emailForm.email });
        })
        .then(res => {
          if (res.code === 200) {
            ElMessage.success(res.msg);
            // 3秒后跳转到登录页
            setTimeout(() => {
              router.push("/login");
            }, 3000);
          } else {
            ElMessage.error("密码重置失败：" + res.msg);
          }
        })
        .catch(() => {
          ElMessage.error("网络异常，请重试");
        })
        .finally(() => {
          resetLoading.value = false;
        });
  });
};

// 组件卸载时清除倒计时定时器
onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer);
});
</script>

<style scoped>
.main {
  width: 100%;
  height: 100vh;
  background-size: cover;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color:#e0efff;
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
  background: linear-gradient(135deg, #1E90FF, #91d1ff);
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

/* 切换按钮组样式 */
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

/* 验证码栏样式 */
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
  background-color: #1E90FF !important;
  border-color: #1E90FF !important;
  color: #fff !important;
  white-space: nowrap;
}

/* 重置密码按钮样式 */
.login-btn {
  width: 100%;
  height: 45px;
  font-size: 18px;
  background-color: #1E90FF !important;
  border-color: #1E90FF !important;
}

.register-link {
  text-align: right;
  margin-top: 10px;
  font-size: 14px;
  color: #666;
}

.register-link a {
  color: #1E90FF;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>