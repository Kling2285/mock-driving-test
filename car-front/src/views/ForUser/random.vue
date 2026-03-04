<template>
  <div class="random-practice-container">
    <!-- 左右布局容器 -->
    <div class="practice-layout">
      <!-- 左侧：题目展示区域 + 解析区域 -->
      <div class="practice-left">
        <div class="practice-header">
          <h2>{{ subject === 1 ? '科目一' : '科目四' }} {{ model.toUpperCase() }} 随机练习</h2><br/>
          <div style="display: flex; align-items: center; gap: 16px;margin-left: 100px">
            <p>当前进度：{{ currentIndex + 1 }} / {{ totalCount || 0 }}</p>
            <el-button type="primary" style="margin-left: 100px"  @click="handleEndPractice(true)">结束本次测试</el-button>
          </div>
        </div>

        <div class="question-card" v-if="currentQuestion">
          <div class="question-title">
            <span class="question-index">{{ currentIndex + 1 }}.</span>
            <span class="question-type" style="margin: 0 8px; color: #666; font-weight: 500;">
              {{
                currentQuestion.questionType === 1 ? '【判断题】' :
                    currentQuestion.questionType === 2 ? '【单选题】' :
                        currentQuestion.questionType === 3 ? '【多选题】' :
                            '【未知题型】'
              }}
            </span>
            <span>{{ currentQuestion.question }}</span>
          </div>

          <!-- 题目图片显示（判断url是否存在且非空字符串，存在才渲染） -->
          <div class="question-img" v-if="currentQuestion.url && currentQuestion.url !== ''">
            <img :src="currentQuestion.url" alt="题目配图" class="question-img-item" />
          </div>

          <!-- 选项区域：动态切换单选/多选组件，已提交题目禁用选择 -->
          <div class="option-list">
            <!-- 单选/判断题：el-radio -->
            <div class="option-item" v-if="currentQuestion.questionType !== 3" v-for="(option, key) in getOptions(currentQuestion)" :key="key">
              <el-radio
                  v-model="userAnswer"
                  :label="key"
                  :disabled="isCurrentSubmitted || !!answerStatus[currentIndex]"
              >
                {{ key }}. {{ option }}
              </el-radio>
            </div>

            <!-- 多选题：el-checkbox -->
            <div class="option-item" v-if="currentQuestion.questionType === 3" v-for="(option, key) in getOptions(currentQuestion)" :key="key">
              <el-checkbox
                  v-model="userMultiAnswer"
                  :label="key"
                  :disabled="isCurrentSubmitted || !!answerStatus[currentIndex]"
              >
                {{ key }}. {{ option }}
              </el-checkbox>
            </div>
          </div>

          <!-- 操作按钮：统一状态判断逻辑，修复点击失效 -->
          <div class="btn-group">
            <el-button @click="prevQuestion" :disabled="currentIndex === 0">上一题</el-button>
            <el-button
                type="primary"
                @click="handleBtnClick"
                :disabled="(isCurrentSubmitted || !!answerStatus[currentIndex]) && (currentIndex === totalCount - 1)"
            >
              {{ (isCurrentSubmitted || !!answerStatus[currentIndex]) ? '下一题' : '提交答案' }}
            </el-button>
          </div>
        </div>

        <!-- 题目解析展示框：已提交（当前题/历史题）+ 有解析且非空就显示 -->
        <div
            class="explain-card"
            v-if="currentQuestion && (isCurrentSubmitted || !!answerStatus[currentIndex]) && currentQuestion.explains && currentQuestion.explains !== ''"
        >
          <div class="explain-title">题目解析</div>
          <div class="explain-content">{{ currentQuestion.explains }}</div>
        </div>
      </div> <!-- 修复：补充 practice-left 的闭合标签 -->

      <!-- 右侧：题号导航区域（已修复索引对齐问题） -->
      <div class="practice-right">
        <div class="question-num-list">
          <div
              class="num-item"
              :class="{
              active: (num-1) === currentIndex,
              correct: answerStatus[num-1] === 'correct',
              wrong: answerStatus[num-1] === 'wrong'
            }"
              v-for="num in totalCount"
              :key="num"
              @click="jumpToQuestion(num - 1)"
          >
            {{ num }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, onUnmounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter, onBeforeRouteLeave } from 'vue-router';
import { addExamRecord } from '@/api/examRecord.js';
// 导入错题相关接口
import { addErrorQuestion } from '@/api/errorQuestion.js'; // 新增错题接口

const router = useRouter();
// 响应式数据
const currentIndex = ref(0); // 当前题目索引（0开始）
const userAnswer = ref(''); // 单选/判断答案（绑定字母A/B/C/D）
const userMultiAnswer = ref([]); // 新增：多选题答案（数组，绑定字母A/B/C/D）
const practiceData = ref(null); // 存储从localStorage获取的练习数据
const currentQuestion = ref(null); // 当前展示的题目
const subject = ref(0); // 科目
const model = ref(''); // 驾照类型
const totalCount = ref(0); // 题目总数
const answerStatus = ref({}); // 答题状态存储（key=题号索引，value=correct/wrong）
const userAnswerRecord = ref({}); // 新增：存储用户每道题的实际答题结果（兼容字符串/数组）
const isCurrentSubmitted = ref(false); // 标记当前题目是否已提交答案
const isRecordSaved = ref(false); // 新增：标记考试记录+错题是否已保存（解决重复提交问题）

// 定义本地存储的key（唯一标识，避免和其他数据冲突）
const PRACTICE_PROGRESS_KEY = 'random_practice_progress';

const letterToNumMap = {
  'A': '1',
  'B': '2',
  'C': '3',
  'D': '4'
};
// 反向映射：数字转字母（用于构造错题数据）
const numToLetterMap = {
  '1': 'A',
  '2': 'B',
  '3': 'C',
  '4': 'D'
};

// 多选答案转数字拼接（如 ['A','B'] → '1,2'）
const multiOptionToNumStr = (optionArr) => {
  if (!Array.isArray(optionArr) || optionArr.length === 0) return '';
  return optionArr.map(opt => letterToNumMap[opt] || '').filter(num => num !== '').join(',');
};

// 数字拼接转多选字母（如 '1,2' → ['A','B']）
const numStrToMultiOption = (numStr) => {
  if (!numStr || numStr === '') return [];
  return numStr.split(',').map(num => numToLetterMap[num] || '').filter(opt => opt !== '');
};

// 监听进度相关数据变化，自动保存到localStorage（实现刷新不丢失）
const savePracticeProgress = () => {
  const progressData = {
    currentIndex: currentIndex.value,
    answerStatus: answerStatus.value,
    userAnswerRecord: userAnswerRecord.value, // 新增：保存用户答题记录（兼容数组）
    subject: subject.value,
    model: model.value,
    totalCount: totalCount.value
  };
  localStorage.setItem(PRACTICE_PROGRESS_KEY, JSON.stringify(progressData));
};

// 监听currentIndex和answerStatus变化，自动保存进度
watch([currentIndex, answerStatus, userAnswerRecord], () => {
  // 只有练习数据初始化完成后，才保存进度
  if (practiceData.value) {
    savePracticeProgress();
  }
}, { deep: true }); // deep: true 监听对象内部变化

/**
 * 批量保存错题到数据库（修复：正确获取用户答案 + 类型匹配）
 * @param {Number} examRecordId - 关联的考试记录ID（exam_record表主键）
 */
const batchSaveErrorQuestions = async (examRecordId) => {
  try {
    const loginUserStr = localStorage.getItem('login_user');
    if (!loginUserStr) {
      ElMessage.warning('请先登录后再保存错题记录');
      return;
    }
    const userInfo = JSON.parse(loginUserStr);
    const userId = userInfo.userId;
    if (!userId) {
      ElMessage.error('用户信息异常，请重新登录');
      return;
    }

    const wrongQuestionIndexes = [];
    const processedApiIds = new Set();
    for (const key in answerStatus.value) {
      if (answerStatus.value[key] === 'wrong') {
        const index = Number(key);
        const question = practiceData.value?.questions[index];
        // 这里也要同步修正字段名：question.apiId
        if (question && !processedApiIds.has(question.apiId)) {
          processedApiIds.add(question.apiId);
          wrongQuestionIndexes.push(index);
        }
      }
    }

    if (wrongQuestionIndexes.length === 0) {
      ElMessage.info('无错题需要保存');
      return;
    }

    const wrongQuestionPromises = [];
    const validExamRecordId = examRecordId || 1;
    for (const index of wrongQuestionIndexes) {
      const question = practiceData.value.questions[index];
      if (!question) continue;

      // 兼容单选/多选答案
      let userAnswerLetter = '';
      const userAnswerVal = userAnswerRecord.value[index] || '';
      if (Array.isArray(userAnswerVal)) {
        // 多选答案：数组转字符串
        userAnswerLetter = userAnswerVal.join(',');
      } else {
        // 单选答案：直接使用
        userAnswerLetter = userAnswerVal;
      }

      const correctAnswerNum = question.answer || '';
      let correctAnswerLetter = '';
      // 兼容多选正确答案
      if (correctAnswerNum.includes(',')) {
        correctAnswerLetter = numStrToMultiOption(correctAnswerNum).join(',');
      } else {
        correctAnswerLetter = numToLetterMap[correctAnswerNum] || '';
      }

      const errorQuestionData = {
        userId: userId,
        // 核心修复：字段名从 question.api_id 改为 question.apiId
        // 同时显式转为字符串，匹配数据库 varchar 类型，避免隐式转换问题
        apiId: question.apiId ? String(question.apiId) : ("default_api_" + index),
        subject: subject.value || 1,
        model: model.value || "C1",
        userAnswer: userAnswerLetter || "无",
        correctAnswer: correctAnswerLetter || "无",
        examRecordId: validExamRecordId,
      };
      wrongQuestionPromises.push(addErrorQuestion(errorQuestionData));
    }

    const results = await Promise.allSettled(wrongQuestionPromises);
    const successCount = results.filter(r => r.status === 'fulfilled').length;
    const failCount = results.filter(r => r.status === 'rejected').length;

    if (successCount > 0) {
      // 可自定义提示
    }
    if (failCount > 0) {
      console.error(`${failCount}道错题保存失败：`, results.filter(r => r.status === 'rejected'));
      ElMessage.warning(`${failCount}道错题保存失败`);
    }
  } catch (error) {
    console.error('批量保存错题失败：', error);
    ElMessage.error('错题保存失败，请稍后重试');
  }
};

/**
 * 核心：统一保存考试记录的方法（修复：防重复提交）
 * @param {Boolean} needJump - 是否需要跳转到result页面
 */
const saveExamRecord = async (needJump = false) => {
  if (isRecordSaved.value) {
    if (needJump) {
      router.push('/umanager/result');
    }
    return;
  }

  try {
    const loginUserStr = localStorage.getItem('login_user');
    if (!loginUserStr) {
      ElMessage.warning('请先登录后再保存考试记录');
      if (needJump) {
        router.push('/login');
      }
      return;
    }
    const userInfo = JSON.parse(loginUserStr);
    const userId = userInfo.userId;
    if (!userId) {
      ElMessage.error('用户信息异常，请重新登录');
      if (needJump) {
        router.push('/login');
      }
      return;
    }

    const doneQuestionCount = Object.keys(answerStatus.value).length;
    if (doneQuestionCount === 0) {
      ElMessage.info('无答题记录，无法保存考试记录');
      if (needJump) {
        router.push('/');
      }
      return;
    }

    if (!subject.value || !model.value || totalCount.value === 0) {
      ElMessage.error('练习数据异常，无法保存考试记录');
      return;
    }

    let correctCount = 0;
    for (const key in answerStatus.value) {
      if (answerStatus.value[key] === 'correct') {
        correctCount++;
      }
    }
    const errorCount = doneQuestionCount - correctCount;
    const score = correctCount;
    const isPass = score >= 90 ? 1 : 0;

    const examRecordData = {
      userId: userId,
      subject: subject.value,
      model: model.value,
      totalQuestion: doneQuestionCount,
      correctCount: correctCount,
      errorCount: errorCount,
      score: score,
      isPass: isPass,
      examTime: new Date().toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit', second: '2-digit' }).replace(/\//g, '-')
    };

    const res = await addExamRecord(examRecordData);
    console.log("考试记录保存响应：", res);
    if (res.code === 200) {
      // 核心：强化 examRecordId 有效性，兜底非空值
      let examRecordId = null;
      // 兼容后端两种返回格式：返回完整对象（res.data.id） / 直接返回ID（res.data）
      if (res.data && typeof res.data === 'object') {
        examRecordId = res.data.id || 1; // 兜底1
      } else if (typeof res.data === 'number' || res.data) {
        examRecordId = Number(res.data) || 1; // 兜底1
      } else {
        examRecordId = 1; // 最终兜底，确保非空
      }
      console.log("最终传递的examRecordId（非空）：", examRecordId);

      // 批量保存错题（此时 examRecordId 必非空）
      await batchSaveErrorQuestions(examRecordId);

      isRecordSaved.value = true;
      if (needJump) {
        router.push('/umanager/result');
      }
    } else {
      ElMessage.error('保存考试记录失败：' + res.msg);
    }
  } catch (error) {
    console.error('保存考试记录失败：', error);
    ElMessage.error('保存考试记录失败，请稍后重试');
  }
};

onMounted(() => {
  initPracticeData();
});

// 修复：离开界面时，仅当未保存记录时才执行保存（解决重复提交）
onBeforeRouteLeave((to, from, next) => {
  if (!isRecordSaved.value) {
    saveExamRecord(false).then(() => {
      next();
    });
  } else {
    next();
  }
});

// 组件销毁时（退出界面）清除本地进度数据
onUnmounted(() => {
  localStorage.removeItem(PRACTICE_PROGRESS_KEY);
  // 重置所有响应式数据
  currentIndex.value = 0;
  answerStatus.value = {};
  userAnswerRecord.value = {};
  isCurrentSubmitted.value = false;
  isRecordSaved.value = false;
  userAnswer.value = '';
  userMultiAnswer.value = []; // 重置多选答案
});

/**
 * 初始化练习数据（修复：恢复用户答题记录，兼容多选）
 */
const initPracticeData = () => {
  // 1. 先获取原始练习题目数据
  const storageQuestionData = localStorage.getItem('randomPracticeQuestions');
  if (!storageQuestionData) {
    ElMessage.error('未获取到练习数据，请返回首页重新开始');
    window.history.back();
    return;
  }
  const parseQuestionData = JSON.parse(storageQuestionData);
  practiceData.value = parseQuestionData;
  subject.value = parseQuestionData.subject;
  model.value = parseQuestionData.model;
  totalCount.value = parseQuestionData.questions.length;

  // 2. 尝试从本地存储恢复进度数据
  const progressStr = localStorage.getItem(PRACTICE_PROGRESS_KEY);
  if (progressStr) {
    try {
      const progressData = JSON.parse(progressStr);
      // 校验进度数据是否匹配当前练习
      if (
          progressData.subject === subject.value &&
          progressData.model === model.value &&
          progressData.totalCount === totalCount.value
      ) {
        // 恢复进度
        currentIndex.value = progressData.currentIndex;
        answerStatus.value = progressData.answerStatus || {};
        userAnswerRecord.value = progressData.userAnswerRecord || {}; // 恢复用户答题记录
      }
    } catch (error) {
      console.error('恢复练习进度失败：', error);
      localStorage.removeItem(PRACTICE_PROGRESS_KEY); // 损坏数据清除
    }
  }

  // 3. 设置当前题目
  currentQuestion.value = parseQuestionData.questions[currentIndex.value];
  // 初始化当前题提交状态
  isCurrentSubmitted.value = !!answerStatus.value[currentIndex.value];
  // 初始化答案（兼容单选/多选）
  const initAnswer = userAnswerRecord.value[currentIndex.value] || '';
  if (currentQuestion.value?.questionType === 3) {
    userMultiAnswer.value = Array.isArray(initAnswer) ? initAnswer : [];
  } else {
    userAnswer.value = initAnswer || '';
  }
};

/**
 * 监听题目索引变化：切换题目时自动识别是否为历史已答题，兼容多选
 */
watch(currentIndex, (newIndex) => {
  // 若切换到历史已答题，标记为已提交；否则重置提交状态
  isCurrentSubmitted.value = !!answerStatus.value[newIndex];
  const targetAnswer = userAnswerRecord.value[newIndex] || '';
  const targetQuestion = practiceData.value?.questions[newIndex];

  // 重置当前答案
  userAnswer.value = '';
  userMultiAnswer.value = [];

  // 恢复对应题型的答案
  if (targetQuestion?.questionType === 3) {
    userMultiAnswer.value = Array.isArray(targetAnswer) ? targetAnswer : [];
  } else {
    userAnswer.value = targetAnswer || '';
  }

  // 更新当前题目
  if (practiceData.value && practiceData.value.questions) {
    currentQuestion.value = practiceData.value.questions[newIndex];
  }
});

/**
 * 获取题目选项（适配判断题/单选题/多选题，对应表中item1-item4）
 */
const getOptions = (question) => {
  const options = {};
  // 仅当选项非空时才添加
  if (question.item1 && question.item1 !== '') options.A = question.item1;
  if (question.item2 && question.item2 !== '') options.B = question.item2;
  if (question.item3 && question.item3 !== '') options.C = question.item3;
  if (question.item4 && question.item4 !== '') options.D = question.item4;
  // 判断题特殊处理（适配正确/错误选项）
  if (question.questionType === 1) {
    options.A = '正确';
    options.B = '错误';
  }
  return options;
};

/**
 * 上一题（切换时保留历史答题状态和解析，兼容多选）
 */
const prevQuestion = () => {
  if (!practiceData.value || !practiceData.value.questions) return;
  if (currentIndex.value > 0) {
    currentIndex.value--;
    const targetQuestion = practiceData.value.questions[currentIndex.value];
    const targetAnswer = userAnswerRecord.value[currentIndex.value] || '';
    // 恢复提交状态和答案
    isCurrentSubmitted.value = !!answerStatus.value[currentIndex.value];
    if (targetQuestion?.questionType === 3) {
      userMultiAnswer.value = Array.isArray(targetAnswer) ? targetAnswer : [];
    } else {
      userAnswer.value = targetAnswer || '';
    }
  }
};

/**
 * 下一题（仅已提交后可点击，切换时重置当前题提交状态，兼容多选）
 */
const nextQuestion = () => {
  if (!practiceData.value || !practiceData.value.questions) return;
  if (currentIndex.value < totalCount.value - 1) {
    currentIndex.value++;
    const targetQuestion = practiceData.value.questions[currentIndex.value];
    const targetAnswer = userAnswerRecord.value[currentIndex.value] || '';
    // 恢复提交状态和答案
    isCurrentSubmitted.value = !!answerStatus.value[currentIndex.value];
    if (targetQuestion?.questionType === 3) {
      userMultiAnswer.value = Array.isArray(targetAnswer) ? targetAnswer : [];
    } else {
      userAnswer.value = targetAnswer || '';
    }
  }
};

/**
 * 提交当前题答案（修复：兼容单选/多选答案，记录用户实际答题结果）
 */
const submitAnswer = () => {
  const targetQuestion = currentQuestion.value;
  // 1. 校验当前题目是否存在
  if (!targetQuestion) {
    ElMessage.error('题目数据异常，请刷新重试');
    return;
  }

  let userAnswerVal = '';
  let isAnswerSelected = false;

  // 2. 区分题型，校验并获取答案
  if (targetQuestion.questionType === 3) {
    // 多选题校验
    if (userMultiAnswer.value.length === 0) {
      ElMessage.warning('请至少选择一个选项');
      return;
    }
    isAnswerSelected = true;
    userAnswerVal = [...userMultiAnswer.value]; // 深拷贝多选数组
  } else {
    // 单选/判断题校验
    if (!userAnswer.value || userAnswer.value === '') {
      ElMessage.warning('请先选择答案');
      return;
    }
    isAnswerSelected = true;
    userAnswerVal = userAnswer.value;
  }

  if (!isAnswerSelected) return;

  // 3. 记录用户实际答题结果（核心修复：解决用户答案丢失问题）
  userAnswerRecord.value[currentIndex.value] = userAnswerVal;

  // 4. 对比正确答案
  const correctAnswer = targetQuestion.answer || '';
  let isCorrect = false;

  if (targetQuestion.questionType === 3) {
    // 多选题：用户答案转数字拼接 vs 正确答案
    const userAnswerNumStr = multiOptionToNumStr(userMultiAnswer.value);
    isCorrect = userAnswerNumStr === correctAnswer;
  } else {
    // 单选/判断题：用户答案转数字 vs 正确答案
    const userAnswerNum = letterToNumMap[userAnswer.value] || '';
    if (!userAnswerNum) {
      ElMessage.error('答案选择异常，请重新选择');
      return;
    }
    isCorrect = userAnswerNum === correctAnswer;
  }

  // 5. 提示答题结果
  if (isCorrect) {
    ElMessage.success('回答正确！');
  } else {
    // 解析正确答案，显示给用户
    let correctTip = '';
    if (targetQuestion.questionType === 3) {
      correctTip = numStrToMultiOption(correctAnswer).join('、');
    } else {
      correctTip = numToLetterMap[correctAnswer] || '';
    }
    ElMessage.error(`回答错误，正确答案是：${correctTip}`);
  }

  // 6. 记录答题状态
  answerStatus.value[currentIndex.value] = isCorrect ? 'correct' : 'wrong';

  // 7. 标记当前题目已提交
  isCurrentSubmitted.value = true;
};

/**
 * 按钮统一点击事件
 */
const handleBtnClick = async () => {
  const isSubmitted = isCurrentSubmitted.value || !!answerStatus.value[currentIndex.value];
  // 情况1：未提交答案 → 执行提交逻辑
  if (!isSubmitted) {
    submitAnswer();
  }
  // 情况2：已提交答案 → 处理下一题/最后一题提示
  else {
    // 判断是否为最后一题
    if (currentIndex.value === totalCount.value - 1) {
      // 统计未答题数量
      const unDoneCount = totalCount.value - Object.keys(answerStatus.value).length;
      const tipText = unDoneCount > 0
          ? `这是最后一题，你还有${unDoneCount}道题未完成，确认后将结束本次测试并保存记录？`
          : `这是最后一题，确认后将结束本次测试并保存记录？`;

      try {
        await ElMessageBox.confirm(
            tipText,
            '最后一题确认',
            {
              confirmButtonText: '确认',
              cancelButtonText: '取消',
              type: 'warning'
            }
        );
        // 用户确认后，保存记录并跳转
        await saveExamRecord(true);
      } catch (error) {
        ElMessage.info('已取消');
      }
    } else {
      nextQuestion();
    }
  }
};

/**
 * 点击题号跳转对应题目（兼容多选）
 * @param {Number} index 题目索引（0开始）
 */
const jumpToQuestion = (index) => {
  if (!practiceData.value || !practiceData.value.questions) return;
  if (index < 0 || index >= totalCount.value) {
    ElMessage.warning('无效的题目编号');
    return;
  }
  currentIndex.value = index;
  const targetQuestion = practiceData.value.questions[index];
  const targetAnswer = userAnswerRecord.value[index] || '';
  // 恢复答案和提交状态
  isCurrentSubmitted.value = !!answerStatus.value[index];
  if (targetQuestion?.questionType === 3) {
    userMultiAnswer.value = Array.isArray(targetAnswer) ? targetAnswer : [];
  } else {
    userAnswer.value = targetAnswer || '';
  }
};

/**
 * 结束本次测试：调用统一保存方法
 */
const handleEndPractice = (needJump = true) => {
  if (!isRecordSaved.value) {
    saveExamRecord(needJump);
  } else {
    if (needJump) {
      router.push('/umanager/result');
    }
  }
};
</script>

<style scoped>
.random-practice-container {
  width: 90%;
  margin: 0 auto;
  padding: 20px 0;
}

/* 左右布局容器：弹性布局 */
.practice-layout {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

/* 左侧：题目区域（占比70%） */
.practice-left {
  flex: 7; /* 左侧占7份 */
}

.practice-header {
  text-align: center;
  margin-bottom: 10px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.question-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.question-title {
  font-size: 18px;
  margin-bottom: 20px;
  line-height: 1.5;
}

.question-index {
  color: #409EFF;
  font-weight: bold;
  margin-right: 8px;
}

/* 题目图片样式 */
.question-img {
  margin: 15px 0;
  text-align: center;
}

.question-img-item {
  max-width: 100%;
  max-height: 300px;
  border-radius: 4px;
  box-shadow: 0 1px 6px rgba(0,0,0,0.1);
  object-fit: contain;
}

.option-list {
  margin-bottom: 30px;
}

.option-item {
  margin: 10px 0;
  font-size: 16px;
}

.btn-group {
  display: flex;
  justify-content: center;
  gap: 20px;
}

/* 解析框样式 */
.explain-card {
  margin-top: 20px;
  background: #fff;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.explain-title {
  font-size: 16px;
  font-weight: 600;
  color: #409EFF;
  margin-bottom: 10px;
}

.explain-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  white-space: pre-wrap; /* 保留换行符，格式化显示 */
}

/* 右侧：题号导航区域（占比30%） */
.practice-right {
  flex: 3; /* 右侧占3份 */
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  height: fit-content;
}

/* 题号列表：网格布局，每行7个，间距紧凑 */
.question-num-list {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 3px;
}

/* 单个题号样式 */
.num-item {
  width: 35px;
  height: 35px;
  line-height: 35px;
  text-align: center;
  border-radius: 4px;
  background: #f5f7fa;
  color: #666;
  cursor: pointer;
  transition: all 0.2s ease;
  margin: 0 auto;
}

/* 当前题号：蓝色（Element Plus 主色） */
.num-item.active {
  background: #409EFF;
  color: #fff;
  font-weight: bold;
}

/* 答对题号：绿色 */
.num-item.correct {
  background: #67C23A;
  color: #fff;
}

/* 答错题号：红色 */
.num-item.wrong {
  background: #F56C6C;
  color: #fff;
}

/* 题号 hover 效果（未答题/未选中状态） */
.num-item:not(.active):not(.correct):not(.wrong):hover {
  background: #e4e6eb;
}
</style>