<template>
  <div class="random-practice-container">
    <div class="practice-layout">
      <div class="practice-left">
        <div class="practice-header">
          <h2>{{ subject === 1 ? '科目一' : '科目四' }} {{ model.toUpperCase() }} 模拟考试</h2>
          <div style="display: flex; align-items: center; gap: 16px">
            <span class="countdown-text" style="color: #F56C6C; font-size: 16px; font-weight: bold;">
              剩余时间：{{ formattedTime }}
            </span>
            <p style="margin-left: 200px">当前进度：{{ currentIndex + 1 }} / {{ totalCount || 0 }}</p>
            <el-button type="primary"
                       style="margin-left: 200px"
                       @click="handleEndPractice(true)"
                       :loading="isEndPracticeLoading" >
              结束本次测试
            </el-button>
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

          <div class="question-img" v-if="currentQuestion.url && currentQuestion.url !== ''">
            <img :src="currentQuestion.url" alt="题目配图" class="question-img-item" />
          </div>

          <div class="option-list">
            <div class="option-item" v-if="currentQuestion.questionType !== 3" v-for="(option, key) in getOptions(currentQuestion)" :key="key">
              <el-radio
                  v-model="userAnswer"
                  :label="key"
                  :disabled="isCurrentSubmitted || !!answerStatus[currentIndex]"
              >
                {{ key }}. {{ option }}
              </el-radio>
            </div>

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

        <div
            class="explain-card"
            v-if="currentQuestion && (isCurrentSubmitted || !!answerStatus[currentIndex]) && currentQuestion.explains && currentQuestion.explains !== ''"
        >
          <div class="explain-title">题目解析</div>
          <div class="explain-content">{{ currentQuestion.explains }}</div>
        </div>
      </div>

      <!-- 右侧：题号导航区域（新增考生信息） -->
      <div class="practice-right">
        <!-- 考生信息展示 -->
        <div class="user-info-panel">
          <div class="user-avatar">
            <img src="https://ts4.tc.mm.bing.net/th/id/OIP-C.IykEwu6UUNOvq9LFU0d3kwAAAA?rs=1&pid=ImgDetMain&o=7&rm=3/80/80" alt="用户头像" class="avatar-img">
          </div>
          <div class="user-base-info">
            <p class="user-name">{{ userInfo.nickname || userInfo.username }}</p>
            <p class="user-id">用户ID：{{ userInfo.userId }}</p>
          </div>
        </div>

        <!-- 题号列表 -->
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
import { ref, onMounted, watch, onUnmounted, computed } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter, onBeforeRouteLeave } from 'vue-router';
import { addExamRecord } from '@/api/examRecord.js';
import { addErrorQuestion } from '@/api/errorQuestion.js';

const router = useRouter();
const currentIndex = ref(0);
const userAnswer = ref('');
const userMultiAnswer = ref([]);
const practiceData = ref(null);
const currentQuestion = ref(null);
const subject = ref(0);
const model = ref('');
const totalCount = ref(0);
const answerStatus = ref({});
const isCurrentSubmitted = ref(false);
const PRACTICE_PROGRESS_KEY = 'mock_exam_progress';
const isExamRecordSaved = ref(false);
const userAnswerRecord = ref({});
const isAutoSubmitTriggered = ref(false);
const isEndPracticeLoading = ref(false);

// 考生信息响应式数据
const userInfo = ref({
  userId: '',
  username: '',
  nickname: '',
  userType: ''
});

const COUNTDOWN_STORAGE_KEY = 'mock_exam_remaining_seconds';
const totalSeconds = ref(45 * 60);
const countdownTimer = ref(null);
const formattedTime = ref('');

const formatTime = (seconds) => {
  const min = Math.floor(seconds / 60);
  const sec = seconds % 60;
  return `${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}`;
};

const saveRemainingSeconds = (seconds) => {
  localStorage.setItem(COUNTDOWN_STORAGE_KEY, seconds.toString());
};

const getRemainingSeconds = () => {
  const storedSeconds = localStorage.getItem(COUNTDOWN_STORAGE_KEY);
  return storedSeconds ? Number(storedSeconds) : 45 * 60;
};

const startCountdown = () => {
  totalSeconds.value = getRemainingSeconds();
  formattedTime.value = formatTime(totalSeconds.value);
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value);
  }
  countdownTimer.value = setInterval(() => {
    if (totalSeconds.value <= 0) {
      clearInterval(countdownTimer.value);
      localStorage.removeItem(COUNTDOWN_STORAGE_KEY);
      ElMessage.warning('考试时间已到，自动结束本次测试！');
      handleEndPractice(true);
      return;
    }
    totalSeconds.value--;
    saveRemainingSeconds(totalSeconds.value);
    formattedTime.value = formatTime(totalSeconds.value);
  }, 1000);
};

const letterToNumMap = {
  'A': '1',
  'B': '2',
  'C': '3',
  'D': '4'
};
const numToLetterMap = {
  '1': 'A',
  '2': 'B',
  '3': 'C',
  '4': 'D'
};

const multiOptionToNumStr = (optionArr) => {
  if (!Array.isArray(optionArr) || optionArr.length === 0) return '';
  return optionArr.map(opt => letterToNumMap[opt] || '').filter(num => num !== '').join(',');
};

const numStrToMultiOption = (numStr) => {
  if (!numStr || numStr === '') return [];
  return numStr.split(',').map(num => numToLetterMap[num] || '').filter(opt => opt !== '');
};

const errorCount = computed(() => {
  let count = 0;
  for (const key in answerStatus.value) {
    if (answerStatus.value[key] === 'wrong') {
      count++;
    }
  }
  return count;
});

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
        if (question && !processedApiIds.has(question.apiId)) {
          processedApiIds.add(question.apiId);
          wrongQuestionIndexes.push(index);
        }
      }
    }

    const uniqueWrongQuestionMap = new Map();
    wrongQuestionIndexes.forEach(index => {
      const question = practiceData.value.questions[index];
      if (!question) return;
      const compositeUniqueKey = `${userId}-${question.apiId}-${subject.value}-${model.value}`;
      if (!uniqueWrongQuestionMap.has(compositeUniqueKey)) {
        uniqueWrongQuestionMap.set(compositeUniqueKey, index);
      }
    });
    // 提取去重后的题目索引列表
    const finalWrongIndexes = Array.from(uniqueWrongQuestionMap.values());
    // ==========================================

    // 后续逻辑使用去重后的 finalWrongIndexes，替换原来的 wrongQuestionIndexes
    if (finalWrongIndexes.length === 0) {
      return;
    }

    const wrongQuestionPromises = [];
    const validExamRecordId = examRecordId || 1;
    // 遍历去重后的索引
    for (const index of finalWrongIndexes) {
      const question = practiceData.value.questions[index];
      if (!question) continue;

      let userAnswerLetter = '';
      const userAnswerVal = userAnswerRecord.value[index] || '';
      if (Array.isArray(userAnswerVal)) {
        // 多选题答案拼接逻辑不变（正常工作，无需修改）
        userAnswerLetter = userAnswerVal.join(',');
      } else {
        userAnswerLetter = userAnswerVal;
      }

      const correctAnswerNum = question.answer || '';
      let correctAnswerLetter = '';
      if (correctAnswerNum.includes(',')) {
        correctAnswerLetter = numStrToMultiOption(correctAnswerNum).join(',');
      } else {
        correctAnswerLetter = numToLetterMap[correctAnswerNum] || '';
      }

      const errorQuestionData = {
        userId: userId,
        apiId: question.apiId ? String(question.apiId) : (`mock_api_${index}`),
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
    }
    if (failCount > 0) {
      console.error(`${failCount}道错题保存失败：`, results.filter(r => r.status === 'rejected'));
      ElMessage.warning(`${failCount}道错题保存失败，请手动查看`);
    }
  } catch (error) {
    console.error('批量保存错题失败：', error);
    ElMessage.error('错题保存失败，请稍后重试');
  }
};

const savePracticeProgress = () => {
  const progressData = {
    currentIndex: currentIndex.value,
    answerStatus: answerStatus.value,
    userAnswerRecord: userAnswerRecord.value,
    subject: subject.value,
    model: model.value,
    totalCount: totalCount.value
  };
  localStorage.setItem(PRACTICE_PROGRESS_KEY, JSON.stringify(progressData));
};

watch(errorCount, (newCount) => {
  if (newCount > 10 && !isAutoSubmitTriggered.value && !isExamRecordSaved.value) {
    isAutoSubmitTriggered.value = true;
    if (countdownTimer.value) {
      clearInterval(countdownTimer.value);
    }
    ElMessageBox.confirm(
        `当前错题数量为${newCount}题，已超过10题，将自动结束测试并保存记录！`,
        '错题超限提示',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'error',
          closeOnClickModal: false,
          draggable: false
        }
    ).then(async () => {
      await saveExamRecord(true);
    }).catch(() => {
      ElMessage.warning('已强制结束测试，正在保存记录...');
      saveExamRecord(true);
    });
  }
}, { immediate: true });

watch([currentIndex, answerStatus, userAnswerRecord], () => {
  if (practiceData.value) {
    savePracticeProgress();
  }
}, { deep: true });

const saveExamRecord = async (needJump = false) => {
  if (isExamRecordSaved.value) {
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
      if (needJump) {
        router.push('/');
      }
      return;
    }

    if (!subject.value || !model.value || totalCount.value === 0) {
      ElMessage.error('考试数据异常，无法保存考试记录');
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
    if (res.code === 200) {
      isExamRecordSaved.value = true;

      let examRecordId = null;
      if (res.data && typeof res.data === 'object') {
        examRecordId = res.data.id || 1;
      } else if (typeof res.data === 'number' || res.data) {
        examRecordId = Number(res.data) || 1;
      } else {
        examRecordId = 1;
      }
      await batchSaveErrorQuestions(examRecordId);

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

// 加载本地存储的用户信息
const loadUserInfo = () => {
  try {
    const localUserStr = localStorage.getItem('login_user');
    if (localUserStr) {
      const localUser = JSON.parse(localUserStr);
      userInfo.value = {
        userId: localUser.userId || '',
        username: localUser.username || '',
        nickname: localUser.nickname || '',
        userType: localUser.userType || ''
      };
    }
  } catch (error) {
    console.error('解析用户信息失败：', error);
    userInfo.value = {
      userId: '',
      username: '',
      nickname: '未知用户',
      userType: ''
    };
  }
};

onMounted(() => {
  loadUserInfo();
  initPracticeData();
  startCountdown();
});

onBeforeRouteLeave((to, from, next) => {
  saveExamRecord(false).then(() => {
    next();
  });
});

onUnmounted(() => {
  if (countdownTimer.value) {
    clearInterval(countdownTimer.value);
  }
  localStorage.removeItem(COUNTDOWN_STORAGE_KEY);
  localStorage.removeItem(PRACTICE_PROGRESS_KEY);
  localStorage.removeItem('mockExamQuestions');
  currentIndex.value = 0;
  answerStatus.value = {};
  userAnswerRecord.value = {};
  isCurrentSubmitted.value = false;
  isExamRecordSaved.value = false;
  isAutoSubmitTriggered.value = false;
  userAnswer.value = '';
  userMultiAnswer.value = [];
  totalSeconds.value = 45 * 60;
  formattedTime.value = '';
});

const initPracticeData = () => {
  const storageQuestionData = localStorage.getItem('mockExamQuestions');
  if (!storageQuestionData) {
    window.history.back();
    return;
  }
  const parseQuestionData = JSON.parse(storageQuestionData);
  practiceData.value = parseQuestionData;
  subject.value = parseQuestionData.subject;
  model.value = parseQuestionData.model;
  totalCount.value = parseQuestionData.questions.length;

  const progressStr = localStorage.getItem(PRACTICE_PROGRESS_KEY);
  if (progressStr) {
    try {
      const progressData = JSON.parse(progressStr);
      if (
          progressData.subject === subject.value &&
          progressData.model === model.value &&
          progressData.totalCount === totalCount.value
      ) {
        currentIndex.value = progressData.currentIndex;
        answerStatus.value = progressData.answerStatus || {};
        userAnswerRecord.value = progressData.userAnswerRecord || {};
      }
    } catch (error) {
      console.error('恢复模拟考试进度失败：', error);
      localStorage.removeItem(PRACTICE_PROGRESS_KEY);
    }
  }

  currentQuestion.value = parseQuestionData.questions[currentIndex.value];
  isCurrentSubmitted.value = !!answerStatus.value[currentIndex.value];
  const initAnswer = userAnswerRecord.value[currentIndex.value] || '';
  if (currentQuestion.value?.questionType === 3) {
    userMultiAnswer.value = Array.isArray(initAnswer) ? initAnswer : [];
  } else {
    userAnswer.value = initAnswer || '';
  }
};

watch(currentIndex, (newIndex) => {
  const targetQuestion = practiceData.value?.questions[newIndex];
  const targetAnswer = userAnswerRecord.value[newIndex] || '';

  userAnswer.value = '';
  userMultiAnswer.value = [];

  isCurrentSubmitted.value = !!answerStatus.value[newIndex];
  if (targetQuestion?.questionType === 3) {
    userMultiAnswer.value = Array.isArray(targetAnswer) ? targetAnswer : [];
  } else {
    userAnswer.value = targetAnswer || '';
  }

  if (practiceData.value && practiceData.value.questions) {
    currentQuestion.value = practiceData.value.questions[newIndex];
  }
});

const getOptions = (question) => {
  const options = {};
  if (question.item1 && question.item1 !== '') options.A = question.item1;
  if (question.item2 && question.item2 !== '') options.B = question.item2;
  if (question.item3 && question.item3 !== '') options.C = question.item3;
  if (question.item4 && question.item4 !== '') options.D = question.item4;
  if (question.questionType === 1) {
    options.A = '正确';
    options.B = '错误';
  }
  return options;
};

const prevQuestion = () => {
  if (!practiceData.value || !practiceData.value.questions) return;
  if (currentIndex.value > 0) {
    currentIndex.value--;
    const targetQuestion = practiceData.value.questions[currentIndex.value];
    const targetAnswer = userAnswerRecord.value[currentIndex.value] || '';
    isCurrentSubmitted.value = !!answerStatus.value[currentIndex.value];
    if (targetQuestion?.questionType === 3) {
      userMultiAnswer.value = Array.isArray(targetAnswer) ? targetAnswer : [];
    } else {
      userAnswer.value = targetAnswer || '';
    }
  }
};

const nextQuestion = () => {
  if (!practiceData.value || !practiceData.value.questions) return;
  if (currentIndex.value < totalCount.value - 1) {
    currentIndex.value++;
    const targetQuestion = practiceData.value.questions[currentIndex.value];
    const targetAnswer = userAnswerRecord.value[currentIndex.value] || '';
    isCurrentSubmitted.value = !!answerStatus.value[currentIndex.value];
    if (targetQuestion?.questionType === 3) {
      userMultiAnswer.value = Array.isArray(targetAnswer) ? targetAnswer : [];
    } else {
      userAnswer.value = targetAnswer || '';
    }
  }
};

const submitAnswer = () => {
  const targetQuestion = currentQuestion.value;
  if (!targetQuestion) {
    ElMessage.error('题目数据异常，请刷新重试');
    return;
  }

  let userAnswerVal = '';
  let isAnswerSelected = false;

  if (targetQuestion.questionType === 3) {
    if (userMultiAnswer.value.length === 0) {
      ElMessage.warning('请至少选择一个选项');
      return;
    }
    isAnswerSelected = true;
    userAnswerVal = [...userMultiAnswer.value];
  } else {
    if (!userAnswer.value || userAnswer.value === '') {
      ElMessage.warning('请先选择答案');
      return;
    }
    isAnswerSelected = true;
    userAnswerVal = userAnswer.value;
  }

  if (!isAnswerSelected) return;

  userAnswerRecord.value[currentIndex.value] = userAnswerVal;

  const correctAnswer = targetQuestion.answer || '';
  let isCorrect = false;

  if (targetQuestion.questionType === 3) {
    const userAnswerNumStr = multiOptionToNumStr(userMultiAnswer.value);
    isCorrect = userAnswerNumStr === correctAnswer;
  } else {
    const userAnswerNum = letterToNumMap[userAnswer.value] || '';
    if (!userAnswerNum) {
      ElMessage.error('答案选择异常，请重新选择');
      return;
    }
    isCorrect = userAnswerNum === correctAnswer;
  }

  if (isCorrect) {
    ElMessage.success('回答正确！');
  } else {
    let correctTip = '';
    if (targetQuestion.questionType === 3) {
      correctTip = numStrToMultiOption(correctAnswer).join('、');
    } else {
      correctTip = numToLetterMap[correctAnswer] || '';
    }
    ElMessage.error(`回答错误，正确答案是：${correctTip}`);
  }

  answerStatus.value[currentIndex.value] = isCorrect ? 'correct' : 'wrong';
  isCurrentSubmitted.value = true;

  if (errorCount.value > 10 && !isAutoSubmitTriggered.value && !isExamRecordSaved.value) {
    isAutoSubmitTriggered.value = true;
    if (countdownTimer.value) {
      clearInterval(countdownTimer.value);
    }
    saveExamRecord(true);
  }
};

const handleBtnClick = async () => {
  const isSubmitted = isCurrentSubmitted.value || !!answerStatus.value[currentIndex.value];
  if (!isSubmitted) {
    submitAnswer();
  } else {
    if (currentIndex.value === totalCount.value - 1) {
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
        await saveExamRecord(true);
      } catch (error) {
        ElMessage.info('已取消');
      }
    } else {
      nextQuestion();
    }
  }
};

const jumpToQuestion = (index) => {
  if (!practiceData.value || !practiceData.value.questions) return;
  if (index < 0 || index >= totalCount.value) {
    ElMessage.warning('无效的题目编号');
    return;
  }
  currentIndex.value = index;
  const targetQuestion = practiceData.value.questions[index];
  const targetAnswer = userAnswerRecord.value[index] || '';
  isCurrentSubmitted.value = !!answerStatus.value[index];
  if (targetQuestion?.questionType === 3) {
    userMultiAnswer.value = Array.isArray(targetAnswer) ? targetAnswer : [];
  } else {
    userAnswer.value = targetAnswer || '';
  }
};

const handleEndPractice = async (needJump = true) => {
  if (isEndPracticeLoading.value) return;
  isEndPracticeLoading.value = true;

  try {
    await saveExamRecord(needJump);
  } finally {
    isEndPracticeLoading.value = false;
  }
};;
</script>

<style scoped>
.countdown-text {
  color: #F56C6C;
  font-size: 20px;
  font-weight: bold;
}

.random-practice-container {
  width: 90%;
  margin: 0 auto;
  padding: 20px 0;
}

.practice-layout {
  display: flex;
  gap: 30px;
  align-items: flex-start;
}

.practice-left {
  flex: 7;
}

.practice-header {
  text-align: center;
  margin-bottom: 10px;
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
  white-space: pre-wrap;
}

.practice-right {
  flex: 3;
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  height: fit-content;
}

/* 考生信息样式 */
.user-info-panel {
  display: flex;
  align-items: center;
  gap: 15px;
  padding-bottom: 15px;
  margin-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.user-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  background-color: #f5f7fa;
}

.user-base-info {
  flex: 1;
}

.user-name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin: 0 0 8px 0;
}

.user-id, .user-exam-subject {
  font-size: 14px;
  color: #666;
  margin: 0 0 4px 0;
}

.question-num-list {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 3px;
}

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
}

.num-item.active {
  background: #409EFF;
  color: #fff;
  font-weight: bold;
}

.num-item.correct {
  background: #67C23A;
  color: #fff;
}

.num-item.wrong {
  background: #F56C6C;
  color: #fff;
}

.num-item:not(.active):not(.correct):not(.wrong):hover {
  background: #e4e6eb;
}
</style>