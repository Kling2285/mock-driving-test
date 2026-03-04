<template>
  <div class="exam-result-container">
    <div class="result-card" v-if="examResult">
      <!-- 左侧：大圆圈显示考试分数 -->
      <div class="score-circle-wrapper">
        <div class="score-circle" :class="{ pass: examResult.isPass === 1, fail: examResult.isPass === 0 }">
          <span class="score-num">{{ examResult.score }}</span>
          <span class="score-unit">分</span>
        </div>
      </div>

      <!-- 右侧：合格状态 + 其他信息 + 按钮 -->
      <div class="right-content">
        <!-- 合格/不合格标识 -->
        <div class="pass-status" :class="{ pass: examResult.isPass === 1, fail: examResult.isPass === 0 }">
          {{ examResult.isPass === 1 ? '考试合格' : '考试不合格' }}
        </div>

        <!-- 核心数据展示 -->
        <div class="core-info">
          <div class="info-item">
            <label>考试科目：</label>
            <span>{{ examResult.subject === 1 ? '科目一' : '科目四' }}</span>
          </div>
          <div class="info-item">
            <label>驾照类型：</label>
            <span>{{ examResult.model.toUpperCase() }}</span>
          </div>
          <div class="info-item">
            <label>做错题数：</label>
            <span class="error-count">{{ examResult.errorCount }} 题</span>
          </div>
          <div class="info-item">
            <label>做对题数：</label>
            <span class="correct-count">{{ examResult.correctCount }} 题</span>
          </div>
          <div class="info-item">
            <label>已做题数：</label>
            <span>{{ examResult.totalQuestion }} 题</span>
          </div>

          <div class="info-item">
            <label>考试时间：</label>
            <span>{{ examResult.examTime }}</span>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="btn-group">
          <el-button type="primary" @click="goToHome">返回首页</el-button>
          <el-button type="success" @click="rePractice">重新练习</el-button>
        </div>
      </div>
    </div>

    <!-- 加载中/无数据提示 -->
    <div class="empty-tip" v-else-if="!loading">
      暂无本次考试记录，请先完成考试
    </div>
    <div class="loading-tip" v-else>
      正在加载考试结果...
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { selectLatestExamRecord } from '@/api/examRecord.js';
import {generateExamPaper} from '@/api/question.js';

const router = useRouter();

// 响应式数据
const examResult = ref(null); // 本次考试记录
const loading = ref(true); // 加载状态

onMounted(() => {
  // 页面加载时获取最新考试记录（本次考试）
  getLatestExamRecord();
});

/**
 * 获取用户最新一条考试记录（即本次结束的考试记录）
 */
const getLatestExamRecord = async () => {
  try {
    // 1. 获取用户信息
    const loginUserStr = localStorage.getItem('login_user');
    if (!loginUserStr) {
      ElMessage.warning('请先登录');
      router.push('/login');
      loading.value = false;
      return;
    }
    const userInfo = JSON.parse(loginUserStr);
    const userId = userInfo.userId;
    if (!userId) {
      ElMessage.error('用户信息异常');
      router.push('/login');
      loading.value = false;
      return;
    }

    // 2. 调用接口查询最新考试记录
    const res = await selectLatestExamRecord({ userId: userId });
    if (res.code === 200) {
      examResult.value = res.data; // 赋值最新考试记录（本次考试）
    } else {
      ElMessage.warning(res.msg);
    }
  } catch (error) {
    console.error('获取考试结果失败：', error);
    ElMessage.error('获取考试结果失败，请稍后重试');
  } finally {
    loading.value = false; // 结束加载
  }
};

/**
 * 返回用户首页
 */
const goToHome = () => {
  router.push('/umanager/uhome');
};

/**
 * 重新练习：复用 startRandomPractice 逻辑，使用本次考试的科目和驾照类型
 */
const rePractice = async () => {
  // 1. 校验是否获取到本次考试记录（确保有 subject 和 model）
  if (!examResult.value) {
    ElMessage.warning('未获取到考试信息，无法重新练习');
    return;
  }

  // 2. 从本次考试记录中获取 subject 和 model（核心：直接复用，无需用户选择）
  const subject = examResult.value.subject;
  const model = examResult.value.model;

  try {
    // 3. 调用 generateExamPaper 接口获取题目（和 startRandomPractice 逻辑一致）
    const res = await generateExamPaper({
      subject: subject,
      model: model
    });

    // 4. 校验返回结果
    if (!res.data || res.data.length === 0) {
      ElMessage.error(`暂无${subject === 1 ? '科目一' : '科目四'}${model.toUpperCase()}驾照类型的题目数据`);
      return;
    }

    // 5. 统计各题型数量（可选：和你的原有逻辑保持一致）
    let judgeCount = 0; // 判断题（题型1）
    let singleChoiceCount = 0; // 单选题（题型2）
    let multiChoiceCount = 0; // 多选题（题型3）
    res.data.forEach(question => {
      if (question.questionType === 1) {
        judgeCount++;
      } else if (question.questionType === 2) {
        singleChoiceCount++;
      } else if (question.questionType === 3) {
        multiChoiceCount++;
      }
    });

    // 6. 对题目进行随机排序（实现“随机练习”效果）
    const shuffleArray = (arr) => {
      // 复用你的随机排序方法（如果全局有定义可直接调用，这里内置一份避免报错）
      let newArr = [...arr];
      for (let i = newArr.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [newArr[i], newArr[j]] = [newArr[j], newArr[i]];
      }
      return newArr;
    };
    const randomQuestions = shuffleArray([...res.data]);

    // 7. 存储随机题目数据（供 /umanager/random 页面获取）
    localStorage.setItem(
        'randomPracticeQuestions',
        JSON.stringify({
          questions: randomQuestions,
          subject: subject,
          model: model
        })
    );

    // 8. 跳转至随机练习页面（和你的原有逻辑一致：/umanager/random）
    router.push('/umanager/random');
  } catch (error) {
    console.error('获取随机练习题目失败：', error);
    ElMessage.error('获取题目失败，请稍后重试');
  }
};
</script>

<style scoped>
/* 样式仅新增/调整布局相关，原有样式保留 */
.exam-result-container {
  width: 70%;
  margin: 50px auto;
  padding: 20px;
  text-align: center;
}

.result-header h2 {
  color: #333;
  margin-bottom: 50px;
  font-size: 24px;
}

.result-card {
  background: #fff;
  padding: 50px;
  border-radius: 12px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 50px;
}

/* 左侧：分数圆圈样式 */
.score-circle-wrapper {
  flex-shrink: 0; /* 防止圆圈被压缩 */
}
.score-circle {
  width: 180px;
  height: 180px;
  border-radius: 50%;
  display: flex;
  flex-direction: row;
  align-items: baseline;
  justify-content: center;
  color: #fff;
  font-weight: bold;
}
.score-circle.pass {
  background-color: #67C23A; /* 合格：绿色 */
  box-shadow: 0 0 20px rgba(103, 194, 58, 0.3);
}
.score-circle.fail {
  background-color: #F56C6C; /* 不合格：红色 */
  box-shadow: 0 0 20px rgba(245, 108, 108, 0.3);
}
.score-num {
  font-size: 60px;
  line-height: 1;
  margin-top: 65px;
}
.score-unit {
  font-size: 20px;
  margin-left: 4px;
  position: relative;
  top: 2px;
  margin-top: 10px;
}

/* 右侧：内容容器 */
.right-content {
  flex: 1; /* 占据剩余宽度 */
  text-align: left;
}

/* 合格/不合格标识（原有样式保留） */
.pass-status {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 30px;
  padding: 15px 0;
  border-radius: 8px;
  text-align: center;
}
.pass-status.pass {
  color: #67C23A;
  background-color: #f0f9f0;
  border: 1px solid #67C23A;
}
.pass-status.fail {
  color: #F56C6C;
  background-color: #fef0f0;
  border: 1px solid #F56C6C;
}

/* 核心信息布局（原有样式保留，移除grid改为默认流式，可选保留grid） */
.core-info {
  text-align: left;
  margin: 0 0 30px 0;
  width: 100%;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.info-item {
  font-size: 16px;
  display: flex;
  align-items: center;
}

.info-item label {
  color: #666;
  margin-right: 10px;
  font-weight: 500;
  width: 80px;
  text-align: right;
}

.correct-count {
  color: #67C23A;
  font-weight: 500;
}

.error-count {
  color: #F56C6C;
  font-weight: 500;
}

/* 按钮组（原有样式保留） */
.btn-group {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

/* 空数据/加载提示（原有样式保留） */
.empty-tip, .loading-tip {
  font-size: 18px;
  color: #666;
  padding: 50px 0;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 16px rgba(0, 0, 0, 0.1);
}
</style>