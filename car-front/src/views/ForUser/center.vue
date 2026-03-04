<template>
  <div class="user-center-container">
    <div v-if="!userInfo" class="no-login-tip">
      <el-empty description="请先登录后再访问个人中心"></el-empty>
      <el-button type="primary" @click="toLoginPage">立即登录</el-button>
    </div>

    <div v-else class="user-info-wrap">
      <!-- 我的个人资料 -->
      <el-card class="user-info-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span>我的个人资料</span>
            <el-button type="primary" icon="Edit" @click="openPwdDialog" style="margin-left: 700px">修改密码</el-button>
            <el-button type="primary" icon="Edit" @click="openEditDialog">修改资料</el-button>
          </div>
        </template>

        <div class="user-info-content">
          <div class="info-list">
            <div class="info-item">
              <label class="info-label">用户ID：</label>
              <span class="info-value">{{ userInfo.userId || '无' }}</span>
            </div>
            <div class="info-item">
              <label class="info-label">用户名：</label>
              <span class="info-value">{{ userInfo.username || '无' }}</span>
            </div>
            <div class="info-item">
              <label class="info-label">用户昵称：</label>
              <span class="info-value">{{ userInfo.nickname || '无' }}</span>
            </div>
            <div class="info-item">
              <label class="info-label">联系电话：</label>
              <span class="info-value">{{ userInfo.phone || '无' }}</span>
            </div>
            <div class="info-item">
              <label class="info-label">电子邮箱：</label>
              <span class="info-value">{{ userInfo.email || '无' }}</span>
            </div>
            <div class="info-item">
              <label class="info-label">用户类型：</label>
              <span class="info-value">{{ userInfo.userType || '普通用户' }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 我的考试记录（新增：最新记录在上，旧记录在下） -->
      <el-card class="exam-record-card" shadow="hover" style="margin-top: 20px;">
        <template #header>
          <div class="card-header">
            <span>我的考试记录</span>
          </div>
        </template>

        <!-- 考试记录列表 -->
        <div class="exam-record-list" v-if="examRecordList.length > 0">
          <!-- 每条考试记录 -->
          <div class="exam-record-item" v-for="(item, index) in examRecordList" :key="index">
            <div class="exam-record-header">
              <span class="exam-subject">科目{{ item.subject }} - {{ item.model }}驾照</span>
              <span class="exam-result" :class="{ passed: item.isPass === 1, failed: item.isPass === 0 || item.isPass === null }">
                {{ item.isPass === 1 ? '考试通过' : '考试未通过' }}
              </span>
            </div>
            <div class="exam-record-content">
              <div class="exam-info-item">
                <label>考试分数：</label>
                <span>{{ item.score || '无' }}</span>
              </div>
              <div class="exam-info-item">
                <label>考试时间：</label>
                <span>{{ formatDate(item.examTime) }}</span>
              </div>
              <!-- 绑定回顾错题点击事件 -->
              <el-button type="primary" style="width: 100px" @click="handleReviewError(item)">回顾错题</el-button>
            </div>
          </div>
        </div>

        <!-- 无考试记录提示 -->
        <div class="no-exam-record" v-else>
          <el-empty description="暂无考试记录"></el-empty>
        </div>
      </el-card>

      <!-- 修改资料弹窗 -->
      <el-dialog
          title="修改个人资料"
          v-model="editDialogVisible"
          width="500px"
          destroy-on-close
          :before-close="handleDialogClose"
      >
        <el-form
            ref="editUserFormRef"
            :model="editUserForm"
            :rules="editUserRules"
            label-width="100px"
            class="edit-user-form"
        >
          <el-form-item label="用户ID" prop="userId">
            <el-input v-model="editUserForm.userId" disabled placeholder="用户ID不可修改"></el-input>
          </el-form-item>
          <el-form-item label="用户名" prop="username">
            <el-input v-model="editUserForm.username" placeholder="请输入用户名（不可与其他用户重复）"></el-input>
          </el-form-item>
          <el-form-item label="用户昵称" prop="nickname">
            <el-input v-model="editUserForm.nickname" placeholder="请输入用户昵称"></el-input>
          </el-form-item>
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="editUserForm.phone" placeholder="请输入11位手机号" maxlength="11"></el-input>
          </el-form-item>
          <el-form-item label="电子邮箱" prop="email">
            <el-input v-model="editUserForm.email" placeholder="请输入有效的电子邮箱"></el-input>
          </el-form-item>
          <!-- 隐藏用户类型，确保提交时携带 -->
          <el-form-item prop="userType" style="display: none;">
            <el-input v-model="editUserForm.userType" disabled></el-input>
          </el-form-item>
        </el-form>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="handleDialogClose">取消</el-button>
            <el-button type="primary" @click="submitEditUserForm" :loading="submitLoading">确认修改</el-button>
          </div>
        </template>
      </el-dialog>

      <!-- 修改密码弹窗（三行输入框：原密码、新密码、确认新密码） -->
      <el-dialog
          title="修改密码"
          v-model="pwdDialogVisible"
          width="400px"
          destroy-on-close
          :before-close="handlePwdDialogClose"
      >
        <el-form
            ref="pwdFormRef"
            :model="pwdForm"
            :rules="pwdRules"
            label-width="100px"
            class="pwd-form"
        >
          <el-form-item label="原有密码" prop="oldPwd">
            <el-input v-model="pwdForm.oldPwd" type="password" placeholder="请输入原有密码" show-password></el-input>
          </el-form-item>
          <el-form-item label="新密码" prop="newPwd">
            <el-input v-model="pwdForm.newPwd" type="password" placeholder="请输入6-20位新密码" show-password maxlength="20"></el-input>
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPwd">
            <el-input v-model="pwdForm.confirmPwd" type="password" placeholder="请再次输入新密码" show-password maxlength="20"></el-input>
          </el-form-item>
        </el-form>

        <template #footer>
          <div class="dialog-footer">
            <el-button @click="handlePwdDialogClose">取消</el-button>
            <el-button type="primary" @click="submitPwdForm" :loading="pwdSubmitLoading">确认修改</el-button>
          </div>
        </template>
      </el-dialog>

      <!-- 新增：错题回顾弹窗 -->
      <el-dialog
          v-model="errorQuestionDialogVisible"
          width="80%"
          max-width="1200px"
          destroy-on-close
          :close-on-click-modal="true"
          scrollbar-width="0"
          class="error-dialog"
      >
      <!-- 弹窗主体容器：固定头部和底部，中间滚动 -->
      <div class="dialog-main-container">
        <!-- 无错题提示 -->
        <div v-if="currentErrorQuestions.length === 0" class="no-error-question">
          <el-empty description="本次考试暂无错题"></el-empty>
        </div>
        <!-- 错题列表：仅中间区域滚动（已保留原有样式，仅归属到滚动容器） -->
        <div v-else class="error-question-list">
          <el-card
              class="error-question-card"
              shadow="hover"
              v-for="(q, idx) in currentErrorQuestions"
              :key="idx"
          >
            <!-- 卡片内部内容不变，完全保留你原有代码 -->
            <template #header>
              <div class="question-header">
                <span class="question-no">错题 {{ idx + 1 }}</span>
                <span class="question-type" v-if="q.questionType">
                  <span v-if="q.questionType === 1">判断题</span>
                  <span v-else-if="q.questionType === 2">单选题</span>
                  <span v-else-if="q.questionType === 3">多选题</span>
                  <span v-else>未知题型</span>
                </span>
              </div>
            </template>
            <div class="question-content">
              {{ q.questionContent || '无题目内容' }}
            </div>
            <div class="question-img" v-if="q.questionUrl && q.questionUrl.trim() !== ''">
              <img :src="q.questionUrl" alt="题目配图" class="question-img-item" />
            </div>
            <div class="question-options" v-if="q.optionA || q.optionB || q.optionC || q.optionD">
              <div class="option-item" :class="{ 'user-answer': q.userAnswer === 'A' }">
                A. {{ q.optionA || '' }}
              </div>
              <div class="option-item" :class="{ 'user-answer': q.userAnswer === 'B' }">
                B. {{ q.optionB || '' }}
              </div>
              <div class="option-item" :class="{ 'user-answer': q.userAnswer === 'C' }">
                C. {{ q.optionC || '' }}
              </div>
              <div class="option-item" :class="{ 'user-answer': q.userAnswer === 'D' }">
                D. {{ q.optionD || '' }}
              </div>
            </div>
            <div class="answer-info">
              <div class="user-answer-info">
                你的答案：<span class="user-answer-text">{{ q.userAnswer || '未作答' }}</span>
              </div>
              <div class="correct-answer-info">
                正确答案：<span class="correct-answer-text">{{ q.correctAnswer || '无' }}</span>
              </div>
            </div>
            <div class="question-analysis" v-if="q.analysis">
              <label>解析：</label>
              <span>{{ q.analysis }}</span>
            </div>
          </el-card>
        </div>
      </div>
      <!-- 弹窗底部：固定在弹窗底部 -->
      <template #footer>
        <el-button @click="errorQuestionDialogVisible = false">关闭</el-button>
      </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { updateuser, selectone,updatePassword } from '@/api/user';
import { listExamRecordByUserId } from '@/api/examRecord';
// 导入错题查询接口
import { listWithQuestionByExamRecord } from '@/api/errorQuestion'; // 请确保该接口已在errorQuestion.js中定义

const router = useRouter();

// 核心数据
const userInfo = ref(null);
// 新增：考试记录列表（最新在上，旧在下）
const examRecordList = ref([]);

// 新增：错题相关数据
const errorQuestionDialogVisible = ref(false); // 错题弹窗显示状态
const currentErrorQuestions = ref([]); // 当前考试的错题列表
const queryLoading = ref(false); // 错题查询加载状态

// 修改资料相关
const editDialogVisible = ref(false);
const editUserFormRef = ref(null);
const submitLoading = ref(false);
const editUserForm = reactive({
  userId: '',
  username: '',
  nickname: '',
  phone: '',
  email: '',
  userType: 'user' // 默认赋值，确保传递
});

// 修改密码相关
const pwdDialogVisible = ref(false);
const pwdFormRef = ref(null);
const pwdSubmitLoading = ref(false);
const pwdForm = reactive({
  oldPwd: '', // 原有密码
  newPwd: '', // 新密码
  confirmPwd: '', // 确认新密码
  userId: '' // 关联当前用户ID，用于后端查询
});

// 表单校验规则（优化手机号格式校验）
const editUserRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在2-20个字符之间', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入用户昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '用户昵称长度在2-20个字符之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的11位手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入电子邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
});

// 修改密码校验规则（核心：原密码必填、新密码长度、两次密码一致）
const pwdRules = reactive({
  oldPwd: [
    { required: true, message: '请输入原有密码', trigger: 'blur' }
  ],
  newPwd: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '新密码长度在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPwd: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== pwdForm.newPwd) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
});

// 工具函数：过滤空字段（核心优化：只保留有值的属性）
const filterEmptyFields = (obj) => {
  const result = {};
  for (const key in obj) {
    if (obj.hasOwnProperty(key)) {
      const value = obj[key];
      // 过滤空字符串、null、undefined，保留0/有效字符串等
      if (value !== '' && value !== null && value !== undefined) {
        result[key] = value;
      }
    }
  }
  return result;
};

// 新增：格式化日期（优化显示效果）
const formatDate = (dateStr) => {
  if (!dateStr) return '无';
  const date = new Date(dateStr);
  const year = date.getFullYear();
  const month = (date.getMonth() + 1).toString().padStart(2, '0');
  const day = date.getDate().toString().padStart(2, '0');
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');
  const seconds = date.getSeconds().toString().padStart(2, '0');
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
};

// 新增：查询用户考试记录（最新在上，旧在下）
const getExamRecordList = async () => {
  if (!userInfo.value || !userInfo.value.userId) {
    ElMessage.warning('用户未登录，无法获取考试记录');
    return;
  }
  const userId = userInfo.value.userId;

  try {
    // 调用接口查询该用户所有考试记录
    const res = await listExamRecordByUserId({ userId: userId });
    if (res && res.code === 200) {
      let records = res.data || [];
      // 排序：按考试时间降序（最新记录在前，旧记录在后）
      records.sort((a, b) => {
        // 兼容不同格式的时间字符串
        return new Date(b.examTime) - new Date(a.examTime);
      });
      examRecordList.value = records;
    } else {
      ElMessage.warning(res?.msg || '获取考试记录失败');
      examRecordList.value = [];
    }
  } catch (err) {
    console.error('查询考试记录失败：', err);
    ElMessage.error('查询考试记录失败');
    examRecordList.value = [];
  }
};

// 新增：处理回顾错题点击事件
const handleReviewError = async (examItem) => {
  if (!userInfo.value || !userInfo.value.userId) {
    ElMessage.warning('用户未登录，无法查询错题');
    return;
  }
  // 重置错题列表
  currentErrorQuestions.value = [];
  // 打开弹窗
  errorQuestionDialogVisible.value = true;
  queryLoading.value = true;

  try {
    // 构造查询参数
    const params = {
      userId: userInfo.value.userId,
      subject: examItem.subject,
      model: examItem.model, // 与考试记录中的驾照类型字段一致
      examRecordId: examItem.id // 考试记录ID，关联错题
    };
    // 调用错题查询接口
    const res = await listWithQuestionByExamRecord(params);
    if (res && res.code === 200) {
      currentErrorQuestions.value = res.data || [];
      if (currentErrorQuestions.value.length === 0) {
        ElMessage.info('本次考试暂无错题');
      }
    } else {
      ElMessage.warning(res?.msg || '查询本次考试错题失败');
    }
  } catch (err) {
    console.error('查询错题失败：', err);
    ElMessage.error('查询错题失败，请稍后重试');
  } finally {
    queryLoading.value = false;
  }
};

// 初始化加载用户信息
const loadUserInfo = () => {
  try {
    const localUserStr = localStorage.getItem('login_user');
    if (!localUserStr) {
      userInfo.value = null;
      ElMessage.warning('请先登录');
      return;
    }

    const localUser = JSON.parse(localUserStr);
    userInfo.value = localUser;
    // 给修改密码表单赋值用户ID
    pwdForm.userId = localUser.userId || '';

    // 调用接口刷新最新用户信息
    if (localUser.userId) {
      selectone(localUser.userId).then(res => {
        if (res && res.code === 200 && res.data) {
          userInfo.value = res.data;
          localStorage.setItem('login_user', JSON.stringify(res.data));
          // 同步更新密码表单的用户ID
          pwdForm.userId = res.data.userId || '';
          // 用户信息刷新后，重新查询考试记录
          getExamRecordList();
        }
      }).catch(err => {
        console.error('刷新用户信息失败：', err);
        // 不报错，使用本地存储数据兜底，同时查询考试记录
        getExamRecordList();
      });
    } else {
      getExamRecordList();
    }
  } catch (err) {
    console.error('加载用户信息失败：', err);
    userInfo.value = null;
    ElMessage.error('加载用户信息失败');
    examRecordList.value = [];
  }
};

// ========== 修改资料相关方法 ==========
// 打开编辑弹窗+数据回显
const openEditDialog = () => {
  if (!userInfo.value) {
    ElMessage.warning('请先获取用户信息');
    return;
  }

  // 清空表单并回显数据
  Object.keys(editUserForm).forEach(key => {
    editUserForm[key] = '';
  });

  // 赋值用户信息（回显原有数据，避免表单为空）
  editUserForm.userId = userInfo.value.userId || '';
  editUserForm.username = userInfo.value.username || '';
  editUserForm.nickname = userInfo.value.nickname || '';
  editUserForm.phone = userInfo.value.phone || '';
  editUserForm.email = userInfo.value.email || '';
  editUserForm.userType = userInfo.value.userType || 'user'; // 优先使用用户原有类型

  // 重置表单校验状态
  nextTick(() => {
    if (editUserFormRef.value) {
      editUserFormRef.value.clearValidate();
    }
  });

  editDialogVisible.value = true;
};

// 关闭修改资料弹窗
const handleDialogClose = () => {
  editDialogVisible.value = false;
  // 重置表单和校验状态
  if (editUserFormRef.value) {
    editUserFormRef.value.clearValidate();
  }
  // 清空非只读字段
  editUserForm.username = '';
  editUserForm.nickname = '';
  editUserForm.phone = '';
  editUserForm.email = '';
};

// 提交修改资料表单
const submitEditUserForm = async () => {
  if (!editUserFormRef.value) return;

  // 表单校验
  const valid = await editUserFormRef.value.validate();
  if (!valid) return;

  submitLoading.value = true;
  try {
    // 1. 深拷贝提交数据，避免原表单被修改
    const submitData = { ...editUserForm };
    // 2. 调用更新接口
    const res = await updateuser(submitData);

    if (res && res.code === 200) {
      ElMessage.success('资料修改成功');
      handleDialogClose();

      // 3. 优先使用接口返回的完整用户数据
      if (res.data) {
        userInfo.value = res.data;
        localStorage.setItem('login_user', JSON.stringify(res.data));
      } else {
        // 4. 过滤空字段后再合并
        const validFields = filterEmptyFields(submitData);
        const updatedUser = { ...userInfo.value, ...validFields };
        userInfo.value = updatedUser;
        localStorage.setItem('login_user', JSON.stringify(updatedUser));
      }
    } else {
      ElMessage.error(res?.msg || '资料修改失败');
    }
  } catch (err) {
    console.error('修改资料失败：', err);
    // 针对性报错提示
    if (err.response && err.response.data) {
      const errMsg = err.response.data.msg;
      if (errMsg.includes('账号已被其他用户使用')) {
        ElMessage.error(errMsg);
      } else if (err.response.data.code === 401) {
        ElMessage.error('登录过期，请重新登录');
        localStorage.removeItem('login_user');
        router.push('/login');
      } else {
        ElMessage.error(errMsg || '网络异常，修改失败');
      }
    } else {
      ElMessage.error('网络异常，修改失败');
    }
  } finally {
    submitLoading.value = false;
  }
};

// ========== 修改密码相关方法 ==========
// 打开修改密码弹窗
const openPwdDialog = () => {
  if (!userInfo.value) {
    ElMessage.warning('请先获取用户信息');
    return;
  }

  // 清空密码表单
  pwdForm.oldPwd = '';
  pwdForm.newPwd = '';
  pwdForm.confirmPwd = '';
  // 重置表单校验状态
  nextTick(() => {
    if (pwdFormRef.value) {
      pwdFormRef.value.clearValidate();
    }
  });

  pwdDialogVisible.value = true;
};

// 关闭修改密码弹窗
const handlePwdDialogClose = () => {
  pwdDialogVisible.value = false;
  // 重置表单和校验状态
  if (pwdFormRef.value) {
    pwdFormRef.value.clearValidate();
  }
  // 清空密码表单
  pwdForm.oldPwd = '';
  pwdForm.newPwd = '';
  pwdForm.confirmPwd = '';
};

// 提交修改密码表单
const submitPwdForm = async () => {
  if (!pwdFormRef.value || !pwdForm.userId) return;

  // 表单校验
  const valid = await pwdFormRef.value.validate();
  if (!valid) return;

  pwdSubmitLoading.value = true;
  try {
    // 构造提交参数：用户ID、原密码、新密码（确认密码已在前端校验一致，无需传给后端）
    const submitData = {
      userId: pwdForm.userId,
      oldPwd: pwdForm.oldPwd,
      newPwd: pwdForm.newPwd
    };

    // 调用修改密码接口（后端会验证原密码+加密新密码）
    const res = await updatePassword(submitData);
    if (res && res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录');
      handlePwdDialogClose();
      // 密码修改成功后，清除本地登录信息，跳转登录页
      localStorage.removeItem('login_user');
      setTimeout(() => {
        router.push('/login');
      }, 1500);
    } else {
      ElMessage.error(res?.msg || '密码修改失败');
    }
  } catch (err) {
    console.error('修改密码失败：', err);
    if (err.response && err.response.data) {
      const errMsg = err.response.data.msg;
      // 后端返回原密码错误提示
      if (errMsg.includes('原有密码错误')) {
        ElMessage.error(errMsg);
      } else if (err.response.data.code === 401) {
        ElMessage.error('登录过期，请重新登录');
        localStorage.removeItem('login_user');
        router.push('/login');
      } else {
        ElMessage.error(errMsg || '网络异常，密码修改失败');
      }
    } else {
      ElMessage.error('网络异常，密码修改失败');
    }
  } finally {
    pwdSubmitLoading.value = false;
  }
};

// 跳转登录页
const toLoginPage = () => {
  router.push('/login');
};

// 初始化调用
onMounted(() => {
  loadUserInfo();
  // 初始化时查询考试记录
  nextTick(() => {
    getExamRecordList();
  });
});
</script>

<style scoped>
.user-center-container {
  width: 90%;
  max-width: 1200px;
  margin: 20px auto;
  padding: 10px;
}

.no-login-tip {
  text-align: center;
  padding: 50px 0;
}

.user-info-wrap {
  width: 100%;
}

.user-info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
  gap: 10px; /* 按钮之间增加间距 */
}

.user-info-content {
  padding: 20px 0;
}

.info-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.info-label {
  font-size: 14px;
  color: #666;
  font-weight: 500;
  white-space: nowrap;
}

.info-value {
  font-size: 16px;
  color: #333;
  flex: 1;
}

.edit-user-form, .pwd-form {
  padding: 10px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 新增：考试记录样式 */
.exam-record-card {
  width: 100%;
}

.exam-record-list {
  padding: 10px 0;
}

.exam-record-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 12px;
  background-color: #fff;
  transition: all 0.3s ease;
}

.exam-record-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
}

.exam-record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px dashed #ebeef5;
}

.exam-subject {
  font-size: 16px;
  font-weight: 500;
  color: #333;
}

.exam-result {
  font-size: 14px;
  font-weight: 500;
  padding: 4px 12px;
  border-radius: 12px;
}

.exam-result.passed {
  color: #67c23a;
  background-color: #f0f9ff;
  border: 1px solid #e1f3d8;
}

.exam-result.failed {
  color: #f56c6c;
  background-color: #fef0f0;
  border: 1px solid #fde2e2;
}

.exam-record-content {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.exam-info-item {
  display: flex;
  flex-direction: column;
  font-size: 14px;
}

.exam-info-item label {
  color: #666;
  margin-bottom: 4px;
  font-weight: 500;
}

.exam-info-item span {
  color: #333;
}

.no-exam-record {
  text-align: center;
  padding: 30px 0;
}

/* 新增：错题弹窗样式 */
.error-dialog {
  --el-dialog-max-height: 90%;
  --el-dialog-margin-top: 10px;
}


.dialog-main-container {
  max-height: calc(90vh - 120px);
  overflow-y: auto;
  padding: 0 10px;
}

/* 优化滚动条样式（修复scoped下&嵌套不生效问题，改为直接定义） */
.dialog-main-container::-webkit-scrollbar {
  width: 6px;
}
.dialog-main-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}
.dialog-main-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}
.dialog-main-container::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.error-question-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 10px 0;
}

.no-error-question {
  text-align: center;
  padding: 50px 0;
}

.error-question-card {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 10px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
}

.question-no {
  color: #333;
}

.question-type {
  color: #409eff;
  font-size: 14px;
}

.question-content {
  font-size: 15px;
  color: #333;
  margin: 16px 0;
  line-height: 1.6;
}

.question-options {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 16px 0;
}

.option-item {
  font-size: 14px;
  color: #666;
  padding: 8px 12px;
  border-radius: 4px;
  border: 1px solid #ebeef5;
}

.option-item.user-answer {
  border-color: #f56c6c;
  background-color: #fef0f0;
  color: #f56c6c;
  font-weight: 500;
}

.answer-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin: 16px 0;
  font-size: 14px;
}

.user-answer-info {
  color: #666;
}

.user-answer-text {
  color: #f56c6c;
  font-weight: 500;
}

.correct-answer-info {
  color: #666;
}

.correct-answer-text {
  color: #67c23a;
  font-weight: 500;
}

.question-analysis {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  padding-top: 16px;
  border-top: 1px dashed #ebeef5;
}

.question-analysis label {
  font-weight: 500;
  color: #333;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .info-list {
    grid-template-columns: 1fr;
  }
  .card-header {
    flex-wrap: wrap;
    justify-content: flex-start;
  }
  .exam-record-content {
    grid-template-columns: 1fr;
  }
  /* 移动端适配弹窗高度 */
  .dialog-main-container {
    max-height: calc(85vh - 100px);
  }
}
</style>