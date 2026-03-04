<template>
  <div class="main">
    <div class="login-box">
      <div class="login-right">
        <h1 class="login-title-right" style="margin-bottom: 20px">注册</h1>

        <div class="login-type-group" style="margin-bottom: 30px;">
          <el-button
              class="login-type-btn"
              :class="{ active: activeType === 'phone' }"
              @click="activeType = 'phone'"
          >
            电话注册
          </el-button>
          <el-button
              class="login-type-btn"
              :class="{ active: activeType === 'email' }"
              @click="activeType = 'email'"
          >
            邮箱注册
          </el-button>
        </div>

        <!-- 电话注册表单（横向一行两个输入框，栅格布局，样式优化） -->
        <el-form
            v-if="activeType === 'phone'"
            :model="phoneForm"
            :rules="phoneRules"
            label-width="80px"
            style="width: 100%;"
            ref="phoneFormRef"
            class="login-form"
        >
          <el-row :gutter="16" class="form-row">
            <el-col :span="12">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="phoneForm.username" autofocus placeholder="请输入用户名" class="login-input"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="密码" prop="password">
                <el-input v-model="phoneForm.password" type="password" show-password placeholder="请输入密码" class="login-input"/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16" class="form-row">
            <el-col :span="12">
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="phoneForm.nickname" placeholder="请输入昵称" class="login-input"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="phoneForm.email" placeholder="请输入邮箱" class="login-input"/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16" class="form-row">
            <el-col :span="12">
              <el-form-item label="电话" prop="phone">
                <el-input v-model="phoneForm.phone" placeholder="请输入电话号码" class="login-input"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="验证码" prop="code" class="code-item">
                <el-input
                    v-model="phoneForm.code"
                    placeholder="请输入短信验证码"
                    class="login-input code-input"
                />
                <el-button
                    class="code-btn"
                    :disabled="phoneCodeBtnDisabled"
                    @click="getPhoneCode"
                >
                  {{ phoneCodeBtnText }}
                </el-button>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item class="submit-item">
            <el-button
                type="primary"
                class="login-btn"
                @click="handlePhoneRegister"
                :loading="phoneRegisterLoading"
            >
              注册
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 邮箱注册表单（横向一行两个输入框，栅格布局，样式优化） -->
        <el-form
            v-if="activeType === 'email'"
            :model="emailForm"
            :rules="emailRules"
            label-width="80px"
            style="width: 100%;"
            ref="emailFormRef"
            class="login-form"
        >
          <el-row :gutter="16" class="form-row">
            <el-col :span="12">
              <el-form-item label="用户名" prop="username">
                <el-input v-model="emailForm.username" autofocus placeholder="请输入用户名" class="login-input"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="密码" prop="password">
                <el-input v-model="emailForm.password" type="password" show-password placeholder="请输入密码" class="login-input"/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16" class="form-row">
            <el-col :span="12">
              <el-form-item label="昵称" prop="nickname">
                <el-input v-model="emailForm.nickname" placeholder="请输入昵称" class="login-input"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="电话" prop="phone">
                <el-input v-model="emailForm.phone" placeholder="请输入电话号码" class="login-input"/>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="16" class="form-row">
            <el-col :span="12">
              <el-form-item label="邮箱" prop="email">
                <el-input v-model="emailForm.email" placeholder="请输入邮箱" class="login-input"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="验证码" prop="code" class="code-item">
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
            </el-col>
          </el-row>

          <el-form-item class="submit-item">
            <el-button
                type="primary"
                class="login-btn"
                @click="handleEmailRegister"
                :loading="registerLoading"
            >
              注册
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
import { sendRegisterEmailCode, emailRegister } from "@/api/register.js"

const router = useRouter();
// 可自由切换注册类型（默认电话注册，也可改为email）
const activeType = ref('phone');

// 电话注册表单（还原完整字段，无禁用）
const phoneForm = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  code: ''
});
const phoneFormRef = ref();
// 电话注册相关状态（独立于邮箱注册，避免冲突）
const phoneCodeBtnDisabled = ref(false);
const phoneCodeBtnText = ref('获取验证码');
const phoneRegisterLoading = ref(false);
let phoneCountdownTimer = null;

// 邮箱注册表单（移除冗余字段，适配后端User实体）
const emailForm = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  code: ''
});
const emailFormRef = ref();
// 邮箱注册相关状态
const codeBtnDisabled = ref(false);
const codeBtnText = ref('获取验证码');
const registerLoading = ref(false);
let countdownTimer = null;

// 电话注册校验规则（完整保留，适配后续短信接口）
const phoneRules = reactive({
  username: [
    { required: true, message: '请填写用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请填写密码', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请填写昵称', trigger: 'blur' }
  ],
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

// 邮箱注册校验规则（适配后端参数要求）
const emailRules = reactive({
  username: [
    { required: true, message: '请填写用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请填写密码', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请填写昵称', trigger: 'blur' }
  ],
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
    { required: true, message: '请填写邮箱验证码', trigger: 'blur' }
  ]
});

// 电话注册提交（保留完整逻辑，后续对接phoneRegister接口即可）
const handlePhoneRegister = () => {
  phoneFormRef.value.validate((valid) => {
    if (!valid) return;

    phoneRegisterLoading.value = true;

    // 临时提示：后续对接后端接口
    ElMessage.info("电话注册接口暂未实现，请先实现后端短信注册接口后对接");
    phoneRegisterLoading.value = false;
  });
};

// 获取短信验证码（独立逻辑，后续对接短信发送接口即可）
const getPhoneCode = () => {
  phoneFormRef.value.validateField('phone', (error) => {
    // 手机号校验失败时直接返回
    if (error) {
      ElMessage.warning(error);
      return;
    }

    // 临时逻辑：模拟倒计时（无实际短信发送，仅展示效果）
    ElMessage.info("短信验证码接口暂未实现，已模拟倒计时效果");
    phoneCodeBtnDisabled.value = true;
    let countdown = 60;
    phoneCodeBtnText.value = `重新发送(${countdown}s)`;

    phoneCountdownTimer = setInterval(() => {
      countdown--;
      phoneCodeBtnText.value = `重新发送(${countdown}s)`;
      if (countdown <= 0) {
        clearInterval(phoneCountdownTimer);
        phoneCodeBtnDisabled.value = false;
        phoneCodeBtnText.value = '获取验证码';
      }
    }, 1000);
  });
};

// 获取邮箱验证码（修复校验逻辑，避免无效请求）
const getEmailCode = () => {
  emailFormRef.value.validateField('email', (error) => {
    // 邮箱校验失败时直接返回
    if (error) {
      ElMessage.warning(error);
      return;
    }
    // 发送验证码请求
    sendRegisterEmailCode({ email: emailForm.email })
        .then(res => {
          if (res.code === 200) {
            ElMessage.success(res.msg);
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

// 邮箱注册提交逻辑（适配后端接口，正常运行）
const handleEmailRegister = () => {
  emailFormRef.value.validate((valid) => {
    if (!valid) return;

    registerLoading.value = true;
    // 提交邮箱注册请求
    emailRegister(emailForm)
        .then(res => {
          if (res.code === 500) {
            ElMessage.error("注册失败：" + res.msg);
          } else if (res.code === 200) {
            ElMessage.success("注册成功，即将跳转到登录页");
            setTimeout(() => {
              router.push("/login");
            }, 1500); // 缩短跳转时间，提升体验
          }
        })
        .catch(() => {
          ElMessage.error("网络异常，请重试");
        })
        .finally(() => {
          registerLoading.value = false;
        });
  });
};

// 清除所有定时器，避免内存泄漏
onUnmounted(() => {
  if (countdownTimer) clearInterval(countdownTimer);
  if (phoneCountdownTimer) clearInterval(phoneCountdownTimer);
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
  width: 650px; /* 微调宽度，更紧凑 */
  height: auto; /* 自适应高度，避免内容溢出 */
  min-height: 550px; /* 最小高度，保持布局稳定 */
  background-color: white;
  border-radius: 12px; /* 圆角优化，更美观 */
  padding: 0;
  box-shadow: 0 6px 24px rgba(0,0,0,0.08); /* 阴影优化，更柔和 */
  display: flex;
  overflow: hidden;
}

.login-right{
  width: 100%; /* 独占宽度，移除左侧冗余布局 */
  padding: 40px 50px; /* 内边距优化，更舒适 */
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.login-title-right {
  text-align: center;
  font-size: 24px; /* 字体放大，更醒目 */
  font-weight: 600;
  color: #333;
  margin-bottom: 25px !important;
}

/* 切换按钮组样式 */
.login-type-group {
  display: flex;
  gap: 0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 35px !important;
}

:deep(.login-type-btn) {
  flex: 1;
  height: 42px; /* 按钮高度微调 */
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

/* 表单行样式，统一间距 */
.form-row {
  margin-bottom: 20px; /* 统一行间距，替代内联样式 */
}

.login-input {
  height: 42px; /* 输入框高度微调 */
  width: 100%;
  border-radius: 6px; /* 输入框圆角优化 */
  border: 1px solid #e5e5e5; /* 边框颜色优化，更柔和 */
  transition: border-color 0.2s ease;
}

/* 输入框聚焦样式优化 */
:deep(.login-input:focus) {
  border-color:#1E90FF !important;
  box-shadow: 0 0 0 2px rgba(251, 0, 0, 0.1) !important;
}

/* 验证码栏样式 */
.code-item {
  display: flex;
  align-items: center;
  gap: 12px; /* 间距微调 */
  width: 100%;
}

.code-input {
  flex: 1;
  height: 42px;
  border-radius: 6px;
  border: 1px solid #e5e5e5;
}

.code-btn {
  width: 80px; /* 按钮宽度微调 */
  height: 42px; /* 与输入框高度一致 */
  background-color: #1E90FF !important;
  border-color: #0d9ef4 !important;
  color: #fff !important;
  white-space: nowrap;
  border-radius: 6px !important; /* 按钮圆角优化 */
  font-size: 13px;
}

/* 提交按钮容器样式 */
.submit-item {
  margin-top: 10px;
  width: 100%;
}

/* 注册按钮样式 */
.login-btn {
  width: 100%;
  height: 48px; /* 按钮高度微调 */
  font-size: 16px; /* 字体大小优化 */
  background-color: #1E90FF!important;
  border-color: #0d9ef4 !important;
  border-radius: 6px !important; /* 按钮圆角优化 */
}

.register-link {
  text-align: right;
  margin-top: 15px; /* 间距微调 */
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