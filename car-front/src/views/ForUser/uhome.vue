<template>
  <div>
    <div class="carousel-wrapper">
      <el-carousel
          height="400px"
          autoplay
          :interval="6000"
          arrow="hover"
      >
      <el-carousel-item>
        <div class="carousel-item">
          <img :src="carousel1" alt="轮播图1" class="carousel-img" />
        </div>
      </el-carousel-item>
      <el-carousel-item>
        <div class="carousel-item">
          <img :src="carousel2" alt="轮播图2" class="carousel-img" />
        </div>
      </el-carousel-item>
      <el-carousel-item>
        <div class="carousel-item">
          <img :src="carousel3" alt="轮播图3" class="carousel-img" />
        </div>
      </el-carousel-item>
      </el-carousel>
    </div>

    <div class="first">
      <div style="margin-top: 60px; text-align: center; font-size: 50px">学车流程</div>
      <div style="text-align: center;color: #999;">整合学车全流程，为用户提供全方位学车服务</div>
      <div class="subject-container">
        <!-- 科目一：移除 hoverIndex 事件，用 CSS :hover 控制 -->
        <div class="subject-item">
          <div class="subject-icon">
            <el-icon class="icon">
              <EditPen />
            </el-icon>
          </div>
          <div class="subject-title">科目一</div>
          <div class="subject-desc">驾驶员理论考试</div>
        </div>

        <!-- 科目二 -->
        <div class="subject-item">
          <div class="subject-icon">
            <el-icon class="icon">
              <Stopwatch />
            </el-icon>
          </div>
          <div class="subject-title">科目二</div>
          <div class="subject-desc">场地驾驶技能考试科目</div>
        </div>

        <!-- 科目三 -->
        <div class="subject-item">
          <div class="subject-icon">
            <el-icon class="icon">
              <HelpFilled />
            </el-icon>
          </div>
          <div class="subject-title">科目三</div>
          <div class="subject-desc">机动车驾驶人道路考试</div>
        </div>

        <!-- 科目四 -->
        <div class="subject-item">
          <div class="subject-icon">
            <el-icon class="icon">
              <Document />
            </el-icon>
          </div>
          <div class="subject-title">科目四</div>
          <div class="subject-desc">安全文明驾驶常识考试</div>
        </div>

        <!-- 拿本 -->
        <div class="subject-item">
          <div class="subject-icon">
            <el-icon class="icon">
              <UserFilled />
            </el-icon>
          </div>
          <div class="subject-title">拿本</div>
          <div class="subject-desc">开心拿本，安全驾驶</div>
        </div>
      </div>
    </div>

    <div class="second">
      <div style="margin-top: 60px; text-align: center; font-size: 50px">快速练习理论考试</div>
      <div class="practice-container">
        <!-- 科目一卡片 -->
        <div class="practice-card">
          <!-- 卡片标题 -->
          <div class="card-title">
            <el-icon class="title-icon"><Reading /></el-icon>
            <span>科目一交规</span>
          </div>
          <div style="font-size: 15px;color: #999;">
            科目一，又称科目一理论考试、驾驶员理论考试，是机动车驾驶证考核的一部分 。根据《机动车驾驶证申领和使用规定》，考试内容包括驾车理论基础、道路安全法律法规、地方性法规等相关知识。考试形式为上机考试，100道题，90分及以上过关。
          </div>

          <div class="practice-grid" style="margin-top: 20px">
            <!-- 随机练习 -->
            <div class="practice-item" @click="handleRandomPractice(1)">
              <el-icon class="item-icon"><Coin /></el-icon>
              <div class="item-title">随机练习</div>
              <div class="item-desc">按随机顺序做题</div>
            </div>

            <div class="practice-item" @click="handleSpecialPractice(1)">
              <el-icon class="item-icon"><Files /></el-icon>
              <div class="item-title">专项练习</div>
              <div class="item-desc">分门别类重点突破</div>
            </div>
          </div>

          <!-- 模拟考试按钮：改为点击事件（不再直接router-link），统一调用接口逻辑 -->
          <el-button
              type="primary"
              class="mock-exam-btn"
              style="margin-top: 10px; width: 500px;"
              @click="handleMockExam(1)"
          >
            进入模拟考试
          </el-button>
        </div>

        <!-- 科目四卡片 -->
        <div class="practice-card">
          <div class="card-title">
            <el-icon class="title-icon"><DocumentRemove /></el-icon>
            <span>科目四安全文明驾驶</span>
          </div>
          <div style="font-size: 15px;color: #999;">
            科目四，又称科目四理论考试、驾驶员理论考试，是机动车驾驶证考核的一部分。公安部123号令实施后，科目三考试分为两项内容，除了路考，增加了安全文明驾驶考试，俗称“科目四”，考量“车德”。
          </div>

          <div class="practice-grid" style="margin-top: 40px">
            <div class="practice-item" @click="handleRandomPractice(4)">
              <el-icon class="item-icon"><Coin /></el-icon>
              <div class="item-title">随机练习</div>
              <div class="item-desc">按随机顺序做题</div>
            </div>

            <div class="practice-item" @click="handleSpecialPractice(4)">
              <el-icon class="item-icon"><Files /></el-icon>
              <div class="item-title">专项练习</div>
              <div class="item-desc">分门别类重点突破</div>
            </div>
          </div>

          <!-- 模拟考试按钮：改为点击事件，统一调用接口逻辑 -->
          <el-button
              type="primary"
              class="mock-exam-btn"
              style="margin-top: 10px; width: 500px;"
              @click="handleMockExam(4)"
          >
            进入模拟考试
          </el-button>
        </div>
      </div>
    </div>

    <div class="thirdly">
      <div style="height: 100px;background-color: #333333; margin-bottom: -20px"></div>
    </div>

    <!-- 1. 随机练习弹窗（独立弹窗，替代原ElMessageBox） -->
    <el-dialog
        v-model="randomPracticeDialogVisible"
        title="随机练习配置"
        width="400px"
        :close-on-click-modal="false"
    >
      <div class="dialog-tip">请选择你的驾照类型：</div>
      <div class="btn-group" style="margin-top: 20px; justify-content: center; gap: 20px;">
        <el-button type="primary" @click="confirmRandomModel('c1')">C1</el-button>
        <el-button type="primary" @click="confirmRandomModel('c2')">C2</el-button>
      </div>
    </el-dialog>

    <!-- 2. 专项练习弹窗（单个弹窗，同时选择驾照类型和题型，无步骤切换） -->
    <el-dialog
        v-model="specialPracticeDialogVisible"
        title="专项练习配置"
        width="450px"
        :close-on-click-modal="false"
    >
      <!-- 驾照类型选择 -->
      <div class="dialog-section">
        <div class="dialog-tip">第一步：选择驾照类型</div>
        <div class="btn-group" style="margin-top: 10px; margin-bottom: 20px; gap: 20px;">
          <el-button
              type="primary"
              :class="{ 'active-btn': currentModel === 'c1' }"
              @click="currentModel = 'c1'"
          >C1</el-button>
          <el-button
              type="primary"
              :class="{ 'active-btn': currentModel === 'c2' }"
              @click="currentModel = 'c2'"
          >C2</el-button>
        </div>
      </div>

      <!-- 题型选择 -->
      <div class="dialog-section">
        <div class="dialog-tip">第二步：选择专项题型</div>
        <div class="btn-group" style="margin-top: 10px; gap: 15px; flex-wrap: wrap; justify-content: center;">
          <el-button
              type="primary"
              :class="{ 'active-btn': currentQuestionType === 1 }"
              @click="currentQuestionType = 1"
          >判断题</el-button>
          <el-button
              type="primary"
              :class="{ 'active-btn': currentQuestionType === 2 }"
              @click="currentQuestionType = 2"
          >单选题</el-button>
          <el-button
              type="primary"
              :class="{ 'active-btn': currentQuestionType === 3 }"
              @click="currentQuestionType = 3"
          >多选题</el-button>
        </div>
      </div>

      <!-- 确认按钮 -->
      <div style="text-align: center; margin-top: 20px;">
        <el-button type="primary" @click="confirmSpecialPractice" :disabled="!currentModel || !currentQuestionType">
          确认开始练习
        </el-button>
      </div>
    </el-dialog>

    <!-- 模拟考试弹窗：和随机练习弹窗一致，选择驾照类型 -->
    <el-dialog
        v-model="mockExamDialogVisible"
        title="模拟考试配置"
        width="400px"
        :close-on-click-modal="false"
    >
      <div class="dialog-tip">请选择你的驾照类型：</div>
      <div class="btn-group" style="margin-top: 20px; justify-content: center; gap: 20px;">
        <el-button type="primary" @click="confirmMockExamModel('c1')">C1</el-button>
        <el-button type="primary" @click="confirmMockExamModel('c2')">C2</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import carousel1 from '@/assets/image/carousel1.png';
import carousel2 from '@/assets/image/carousel2.png';
import carousel3 from '@/assets/image/carousel3.png';

import { ElIcon, ElDialog, ElButton } from 'element-plus';
import { EditPen,Stopwatch,HelpFilled, Document, UserFilled,Reading,Coin,Files,DocumentRemove } from '@element-plus/icons-vue';

import { ElMessage } from 'element-plus';
import { useRouter } from 'vue-router';
import { ref } from 'vue';
// 导入题目相关接口
import {generateExamPaper,generateSpecialTypeExamPaper} from '@/api/question.js';

const router = useRouter();

// 1. 随机练习弹窗相关变量
const randomPracticeDialogVisible = ref(false);
const currentRandomSubject = ref(0); // 随机练习当前科目

// 2. 专项练习弹窗相关变量（单个弹窗，无需步骤切换）
const specialPracticeDialogVisible = ref(false);
const currentSubject = ref(0); // 专项练习当前科目
const currentModel = ref(''); // 当前选中驾照类型（c1/c2）
const currentQuestionType = ref(''); // 当前选中题型（1/2/3）

// 3. 模拟考试弹窗相关变量
const mockExamDialogVisible = ref(false);
const currentMockExamSubject = ref(0); // 模拟考试当前科目

/**
 * 随机练习点击事件：打开随机练习弹窗
 * @param {Number} subject 科目（1=科目一，4=科目四）
 */
const handleRandomPractice = (subject) => {
  const loginUserStr = localStorage.getItem('login_user');
  if (!loginUserStr) {
    ElMessage.warning('请先登录后再进行练习');
    router.push('/login');
    return;
  }

  // 初始化随机练习弹窗变量
  currentRandomSubject.value = subject;
  randomPracticeDialogVisible.value = true;
};

/**
 * 确认随机练习驾照类型并启动练习
 * @param {String} model 驾照类型（c1/c2）
 */
const confirmRandomModel = async (model) => {
  try {
    const res = await generateExamPaper({
      subject: currentRandomSubject.value,
      model: model
    });

    // 校验返回结果
    if (!res.data || res.data.length === 0) {
      ElMessage.error(`暂无${currentRandomSubject.value === 1 ? '科目一' : '科目四'}${model.toUpperCase()}驾照类型的题目数据`);
      randomPracticeDialogVisible.value = false;
      return;
    }

    // 统计各题型数量
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

    // 对题目进行随机排序
    const randomQuestions = shuffleArray([...res.data]);

    // 存储随机题目数据
    localStorage.setItem(
        'randomPracticeQuestions',
        JSON.stringify({
          questions: randomQuestions,
          subject: currentRandomSubject.value,
          model: model
        })
    );

    // 关闭弹窗并跳转随机练习页面
    randomPracticeDialogVisible.value = false;
    router.push('/umanager/random');
  } catch (error) {
    console.error('获取随机练习题目失败：', error);
    ElMessage.error('获取题目失败，请稍后重试');
    randomPracticeDialogVisible.value = false;
  }
};

/**
 * 专项练习点击事件：打开专项练习弹窗
 * @param {Number} subject 科目（1=科目一，4=科目四）
 */
const handleSpecialPractice = (subject) => {
  // 1. 先校验用户是否登录
  const loginUserStr = localStorage.getItem('login_user');
  if (!loginUserStr) {
    ElMessage.warning('请先登录后再进行专项练习');
    router.push('/login');
    return;
  }

  // 2. 初始化专项练习弹窗变量（重置选中状态）
  currentSubject.value = subject;
  currentModel.value = '';
  currentQuestionType.value = '';
  specialPracticeDialogVisible.value = true; // 打开单个弹窗
};

/**
 * 确认专项练习配置并启动练习
 */
const confirmSpecialPractice = async () => {
  try {
    // 1. 调用专项练习接口
    const res = await generateSpecialTypeExamPaper({
      subject: currentSubject.value,
      model: currentModel.value,
      questionType: currentQuestionType.value
    });

    // 2. 校验返回结果
    if (!res.data || res.data.length === 0) {
      ElMessage.error(`暂无${currentSubject.value === 1 ? '科目一' : '科目四'}${currentModel.value.toUpperCase()}${
          currentQuestionType.value === 1 ? '判断题' : currentQuestionType.value === 2 ? '单选题' : '多选题'
      }的题目数据`);
      specialPracticeDialogVisible.value = false;
      return;
    }

    // 3. 存储专项练习数据
    localStorage.setItem(
        'specialPracticeQuestions',
        JSON.stringify({
          questions: res.data,
          subject: currentSubject.value,
          model: currentModel.value,
          questionType: currentQuestionType.value
        })
    );

    // 4. 关闭弹窗并跳转
    specialPracticeDialogVisible.value = false;
    router.push('/umanager/special');
  } catch (error) {
    console.error('获取专项练习题目失败：', error);
    ElMessage.error('获取专项练习题目失败，请稍后重试');
    specialPracticeDialogVisible.value = false;
  }
};

/**
 * 模拟考试点击事件：打开模拟考试弹窗
 * @param {Number} subject 科目（1=科目一，4=科目四）
 */
const handleMockExam = (subject) => {
  const loginUserStr = localStorage.getItem('login_user');
  if (!loginUserStr) {
    ElMessage.warning('请先登录后再进行模拟考试');
    router.push('/login');
    return;
  }

  // 初始化模拟考试弹窗变量
  currentMockExamSubject.value = subject;
  mockExamDialogVisible.value = true;
};

/**
 * 确认模拟考试驾照类型并启动（和随机练习共用同一接口、同一数据处理逻辑）
 * @param {String} model 驾照类型（c1/c2）
 */
const confirmMockExamModel = async (model) => {
  try {
    // 和随机练习调用同一个接口，传参一致
    const res = await generateExamPaper({
      subject: currentMockExamSubject.value,
      model: model
    });

    // 校验返回结果（逻辑和随机练习一致）
    if (!res.data || res.data.length === 0) {
      ElMessage.error(`暂无${currentMockExamSubject.value === 1 ? '科目一' : '科目四'}${model.toUpperCase()}驾照类型的题目数据`);
      mockExamDialogVisible.value = false;
      return;
    }

    // 统计各题型数量（可选，和随机练习一致）
    let judgeCount = 0;
    let singleChoiceCount = 0;
    let multiChoiceCount = 0;
    res.data.forEach(question => {
      if (question.questionType === 1) {
        judgeCount++;
      } else if (question.questionType === 2) {
        singleChoiceCount++;
      } else if (question.questionType === 3) {
        multiChoiceCount++;
      }
    });

    // 随机排序题目（和随机练习一致）
    const mockExamQuestions = shuffleArray([...res.data]);

    // 存储模拟考试数据（单独命名key，避免和随机练习冲突）
    localStorage.setItem(
        'mockExamQuestions',
        JSON.stringify({
          questions: mockExamQuestions,
          subject: currentMockExamSubject.value,
          model: model
        })
    );

    // 关闭弹窗并跳转至模拟考试页面 /umanager/simulation
    mockExamDialogVisible.value = false;
    router.push('/umanager/simulation');
  } catch (error) {
    console.error('获取模拟考试题目失败：', error);
    ElMessage.error('获取题目失败，请稍后重试');
    mockExamDialogVisible.value = false;
  }
};

/**
 * 启动专项练习（兼容原有逻辑，保留防止报错）
 * @param {Number} subject 科目
 * @param {String} model 驾照类型（c1/c2）
 */
const startSpecialPractice = (subject, model) => {
  try {
    localStorage.setItem(
        'specialPracticeInfo',
        JSON.stringify({
          subject: subject,
          model: model
        })
    );
    router.push('/umanager/special');
  } catch (error) {
    console.error('启动专项练习失败：', error);
    ElMessage.error('启动专项练习失败，请稍后重试');
  }
};

/**
 * 数组随机排序工具函数（Fisher-Yates 洗牌算法，保证随机性）
 * @param {Array} arr 待排序数组
 * @returns {Array} 随机排序后的数组
 */
const shuffleArray = (arr) => {
  for (let i = arr.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [arr[i], arr[j]] = [arr[j], arr[i]];
  }
  return arr;
};
</script>

<style scoped>
.carousel-wrapper {
  width: 100%;
  max-width: 1500px;
  margin: -20px auto;
}
.carousel-item {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.carousel-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 容器：水平排列 + 居中 */
.subject-container {
  display: flex;
  justify-content: center;
  gap: 60px;
  padding: 30px 0;
}

/* 单个科目项：垂直居中 */
.subject-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

/* 圆形图标容器：默认样式 */
.subject-icon {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  border: 1px solid #e5e7eb;
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

/* 关键：鼠标悬停 .subject-item 时，修改内部 .subject-icon 样式（背景变蓝） */
.subject-item:hover .subject-icon {
  background-color: #1E90FF;
  border-color: #1E90FF;
}

/* 图标样式：默认 */
.icon {
  font-size: 40px;
  color: #666;
  transition: color 0.3s ease;
}

/* 关键：鼠标悬停 .subject-item 时，图标变白 */
.subject-item:hover .icon {
  color: #fff;
}

/* 科目标题：默认 */
.subject-title {
  font-size: 30px;
  color: #333;
  transition: color 0.3s ease;
}

/* 关键：鼠标悬停 .subject-item 时，标题变蓝 */
.subject-item:hover .subject-title {
  color: #1E90FF;
}

/* 科目描述：不变 */
.subject-desc {
  font-size: 16px;
  color: #999;
  text-align: center;
}
.practice-container {
  display: flex;
  gap: 40px;
  padding: 20px;
  justify-content: center;
  flex-wrap: wrap; /* 小屏幕自动换行 */
}

/* 单个卡片样式 */
.practice-card {
  width: 45%;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* 卡片标题 */
.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 25px;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
}

.title-icon {
  font-size: 23px;
  color: #1E90FF;
}

/* 练习选项网格（2列2行） */
.practice-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  width: 100%;
}

/* 单个练习选项 */
.practice-item {
  text-align: center;
  padding: 12px;
  border: 1px solid #f0f2f5;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.3s;
  background-color: #f3f3f3;
  margin-bottom: 12px;
}
.practice-item:hover {
  background-color: #f5f7fa;
}

/* 选项图标 */
.item-icon {
  font-size: 35px;
  color: #1E90FF;
  margin-bottom: 8px;
}

/* 选项标题 */
.item-title {
  font-size: 16px;
  font-weight:550;
  color: #333;
  margin-bottom: 4px;
}

.practice-item:hover .item-title {
  color: #1E90FF; /* 主蓝色 */
  transition: color 0.3s ease;
}

/* 选项描述 */
.item-desc {
  font-size: 13px;
  color: #999;
}

/* 模拟考试按钮 */
.mock-exam-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  background-color: #1E90FF;
  border-color: #1E90FF;
  border-radius: 4px;
}

/* 弹窗通用样式 */
.dialog-tip {
  font-size: 16px;
  color: #333;
  text-align: center;
}
.btn-group {
  display: flex;
  justify-content: center;
}
.dialog-section {
  margin-bottom: 10px;
}
/* 选中按钮样式 */
.active-btn {
  background-color: #096dd9 !important;
  border-color: #096dd9 !important;
}
</style>