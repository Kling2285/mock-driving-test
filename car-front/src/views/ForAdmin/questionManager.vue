<template>
  <div>
    <!-- 条件查询区域 -->
    <div class="box">
      <el-form :inline="false" :model="queryParams" class="demo-form-inline">
        <!-- 行1：科目类型 + 驾照类型 -->
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="科目类型">
              <el-select v-model="queryParams.subject" placeholder="请选择科目" clearable style="width: 100%;">
                <el-option label="科目一" value="1" />
                <el-option label="科目四" value="4" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="驾照类型">
              <el-select v-model="queryParams.model" placeholder="请选择驾照类型" clearable style="width: 100%;">
                <el-option label="C1驾照" value="c1" />
                <el-option label="C2驾照" value="c2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="题型">
              <el-select v-model="queryParams.questionType" placeholder="请选择题型" clearable style="width: 100%;">
                <el-option label="判断题" value="1" />
                <el-option label="单选题" value="2" />
                <el-option label="多选题" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <!-- 行2：题型 + 题目关键词 -->
        <el-row :gutter="20" style="margin-top: 10px;">

          <el-col :span="8">
            <el-form-item label="题目关键词">
              <el-input v-model="queryParams.keyword" placeholder="请输入题目内容关键词" clearable style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item>
              <el-button color="#3869f4" plain @click="search">
                <span style="margin-left: 4px;">查询</span>
              </el-button>
              <el-button type="info" plain @click="list00" style="margin-left: 10px;">
                <span style="margin-left: 4px;">重置</span>
              </el-button>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <!-- 操作按钮区 -->
    <div class="box" style="display: flex; align-items: center; gap: 8px;">
      <div class="button-row" style="display: flex; align-items: center; gap: 8px;">
        <el-button type="success" plain @click="handleSync">
          <el-icon><Refresh /></el-icon>同步题库
        </el-button>
        <el-button type="info" plain @click="handleExport">
          <el-icon><Upload /></el-icon>导出
        </el-button>
      </div>
    </div>

    <!-- 表格展示区 -->
    <div>
      <el-table :data="data.tableData"
                style="width: 100%"
                @selection-change="handleSelectionChange ">
        <el-table-column type="selection" prop="id" width="60" />
        <el-table-column prop="id" label="题目ID" width="100" />
        <el-table-column prop="question" label="题目内容" width="200" show-overflow-tooltip />
        <el-table-column
            prop="subject"
            label="科目"
            width="100"
        >
          <template #default="scope">
            {{ scope.row.subject === 1 ? '科目一' : '科目四' }}
          </template>
        </el-table-column>
        <el-table-column prop="model" label="驾照类型" width="100" />
        <el-table-column label="题目配图" width="120">
          <template #default="scope">
            <el-image
                v-if="scope.row.url"
                :src="scope.row.url"
                :preview-src-list="[scope.row.url]"
                fit="cover"
                style="width: 80px; height: 60px; cursor: pointer;"
                placeholder="暂无图片"
            />
            <span v-else style="color: #999; font-size: 12px;">无配图</span>
          </template>
        </el-table-column>
        <el-table-column
            prop="questionType"
            label="题型"
            width="100"
        >
          <template #default="scope">
            <el-tag v-if="scope.row.questionType === 1" type="success">判断题</el-tag>
            <el-tag v-else-if="scope.row.questionType === 2" type="primary">单选题</el-tag>
            <el-tag v-else type="warning">多选题</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 分页组件 -->
    <div class="pagination">
      <el-pagination
          background
          layout="prev, pager, next, jumper, ->, total, sizes"
          v-model:page-size="queryParams.pageSize"
          v-model:current-page="queryParams.pageNum"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          :total="data.total"
          :page-sizes="[5, 10, 20, 50]"
      />
    </div>

    <el-dialog v-model="visible" title="题目详情" width="800">
      <el-form :model="form" :label-width="100" disabled>
        <!-- 题目ID -->
        <el-form-item label="题目ID">
          <el-input v-model="form.id" autocomplete="off" readonly />
        </el-form-item>
        <!-- 题目内容 -->
        <el-form-item label="题目内容">
          <el-input
              v-model="form.question"
              type="textarea"
              :rows="3"
              autocomplete="off"
              readonly
          />
        </el-form-item>
        <!-- 答案 -->
        <el-form-item label="答案">
          <el-input v-model="form.answer" autocomplete="off" readonly />
        </el-form-item>
        <!-- 选项1 -->
        <el-form-item label="选项1">
          <el-input v-model="form.item1" autocomplete="off" readonly />
        </el-form-item>
        <!-- 选项2 -->
        <el-form-item label="选项2">
          <el-input v-model="form.item2" autocomplete="off" readonly />
        </el-form-item>
        <!-- 选项3（非判断题显示） -->
        <el-form-item label="选项3" v-if="form.questionType != 1">
          <el-input v-model="form.item3" autocomplete="off" readonly />
        </el-form-item>
        <!-- 选项4（非判断题显示） -->
        <el-form-item label="选项4" v-if="form.questionType != 1">
          <el-input v-model="form.item4" autocomplete="off" readonly />
        </el-form-item>
        <!-- 题目解析 -->
        <el-form-item label="题目解析">
          <el-input
              v-model="form.explains"
              type="textarea"
              :rows="2"
              autocomplete="off"
              readonly
          />
        </el-form-item>
        <!-- 图片URL -->
        <el-form-item label="图片URL">
          <el-input v-model="form.url" autocomplete="off" readonly />
        </el-form-item>
        <el-form-item label="科目类型">
          <el-input
              :value="form.subject + '' === '1' ? '科目一' : '科目四'"
              autocomplete="off"
              readonly
          />
        </el-form-item>
        <!-- 驾照类型 -->
        <el-form-item label="驾照类型">
          <el-input v-model="form.model" autocomplete="off" readonly />
        </el-form-item>
        <!-- 题型（修复v-model表达式 + 闭合标签） -->
        <el-form-item label="题型">
          <el-input
              :value="(form.questionType + '') === '1' ? '判断题' : (form.questionType + '') === '2' ? '单选题' : '多选题'"
              autocomplete="off"
              readonly
          />
        </el-form-item>
        <!-- API原始ID -->
        <el-form-item label="API原始ID" v-if="form.apiId">
          <el-input v-model="form.apiId" autocomplete="off" readonly />
        </el-form-item>
      </el-form>
      <!-- 弹窗底部 -->
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="visible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>
    <!-- 同步题库弹窗 -->
    <el-dialog v-model="syncVisible" title="同步题库" width="400">
      <el-form :model="syncForm" :label-width="100" ref="syncFormRef">
        <el-form-item label="科目类型" prop="subject">
          <el-select v-model="syncForm.subject" placeholder="请选择科目类型">
            <el-option label="科目一" value="1" />
            <el-option label="科目四" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="驾照类型" prop="model">
          <el-select v-model="syncForm.model" placeholder="请选择驾照类型">
            <el-option label="C1驾照" value="c1" />
            <el-option label="C2驾照" value="c2" />
            <el-option label="A1驾照" value="a1" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="syncVisible = false">取消</el-button>
          <el-button type="primary" @click="submitSync">
            确认同步
          </el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { reactive, ref, nextTick } from 'vue'
// 导入题目管理接口
import {
  selectOneQuestion,
  exportQuestion,
  pageQuestion,
  syncQuestionBank,
  generateExamPaper
} from '@/api/question.js'
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Upload, Refresh
} from '@element-plus/icons-vue'

// 批量删除选中的ID集合
const ids = ref([])
// 弹窗显隐
const visible = ref(false);
const syncVisible = ref(false);
const paperVisible = ref(false);
// 弹窗标题（仅保留，无实际作用）
const title = ref('添加题目');

// 表单数据（用于存储详情）
const form = reactive({
  id: '',
  apiId: '',
  question: '',
  answer: '',
  item1: '',
  item2: '',
  item3: '',
  item4: '',
  explains: '',
  url: '',
  subject: '',
  model: '',
  questionType: ''
});

// 同步题库表单
const syncForm = reactive({
  subject: '',
  model: ''
});

// 生成试卷表单
const paperForm = reactive({
  subject: '',
  model: ''
});

// 表单校验ref（仅保留，无实际作用）

const syncFormRef = ref();

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  subject: '',
  model: '',
  questionType: '',
  keyword: ''
})

// 表单校验规则（仅保留，无实际作用）
const rules = reactive({
  question: [
    { required: true, message: '题目内容不能为空', trigger: 'blur' },
    { min: 5, max: 500, message: '题目内容长度在5-500个字符', trigger: 'blur' }
  ],
  answer: [
    { required: true, message: '答案不能为空', trigger: 'blur' },
    { pattern: /^[A-D1-4]$/, message: '答案请输入A/B/C/D或1/2/3/4', trigger: 'blur' }
  ],
  item1: [
    { required: true, message: '选项1不能为空', trigger: 'blur' }
  ],
  item2: [
    { required: true, message: '选项2不能为空', trigger: 'blur' }
  ],
  subject: [
    { required: true, message: '科目类型不能为空', trigger: 'change' }
  ],
  model: [
    { required: true, message: '驾照类型不能为空', trigger: 'change' }
  ],
  questionType: [
    { required: true, message: '题型不能为空', trigger: 'change' }
  ]
});

// 表格数据
const data = reactive({
  total: 0,
  tableData: []
})

// 获取题目列表（分页查询）
const getQuestionList = async () => {
  try {
    console.log('调用题目分页接口，参数：', queryParams);
    const params = {
      ...queryParams,
    };
    const res = await pageQuestion(params);
    console.log('题目分页接口返回：', res);
    data.total = res.data?.total || 0;
    data.tableData = res.data?.list || [];
  } catch (err) {
    console.error('获取题目列表失败：', err);
    ElMessage.error('获取题目列表失败');
  }
}

// 导出题目数据
const handleExport = () => {
  try {
    exportQuestion({
      subject: queryParams.subject,
      model: queryParams.model,
      questionType: queryParams.questionType
    }).then(res => {
      // 处理文件下载
      const blob = new Blob([res], { type: 'application/vnd.ms-excel' });
      const a = document.createElement('a');
      a.href = URL.createObjectURL(blob);
      a.download = '驾照题目列表.xlsx';
      a.click();
      URL.revokeObjectURL(a.href);
      ElMessage.success('导出成功，文件已下载');
    }).catch(err => {
      console.error('导出题目失败：', err);
      ElMessage.error('导出失败，请检查参数或网络');
    });
  } catch (err) {
    console.error('导出题目失败：', err);
    ElMessage.error('导出失败，请检查参数或网络');
  }
};

// 条件查询
const search = () => {
  queryParams.pageNum = 1;
  getQuestionList();
}

// 分页页码变更
const handleCurrentChange = (val) => {
  queryParams.pageNum = val;
  getQuestionList();
}

// 分页条数变更
const handleSizeChange = (val) => {
  queryParams.pageSize = val;
  queryParams.pageNum = 1;
  getQuestionList();
};

// 重置查询条件
const list00 = () => {
  queryParams.pageNum = 1;
  queryParams.pageSize = 10;
  queryParams.subject = '';
  queryParams.model = '';
  queryParams.questionType = '';
  queryParams.keyword = '';
  getQuestionList();
}

// 表格选中事件
const handleSelectionChange = (selections) => {
  ids.value = selections.map(item => item.id);
}

// 查看详情（保留原有方法名，仅修改逻辑为详情展示）
const handleEdit = (row) => {
  console.log('调用单个查询题目接口（详情），ID：', row.id);
  selectOneQuestion(row.id).then(res => {
    console.log('单个查询题目接口返回（详情）：', res);
    if (res.data) {
      // 赋值到表单（用于详情展示）
      form.id = res.data.id || '';
      form.apiId = res.data.apiId || '';
      form.question = res.data.question || '';
      form.answer = res.data.answer || '';
      form.item1 = res.data.item1 || '';
      form.item2 = res.data.item2 || '';
      form.item3 = res.data.item3 || '';
      form.item4 = res.data.item4 || '';
      form.explains = res.data.explains || '';
      form.url = res.data.url || '';
      form.subject = res.data.subject?.toString() || '';
      form.model = res.data.model || '';
      form.questionType = res.data.questionType?.toString() || '';
      // 打开详情弹窗
      visible.value = true;
    } else {
      ElMessage.error('获取题目信息失败');
    }
  }).catch(err => {
    console.error('获取题目信息失败：', err);
    ElMessage.error('获取题目信息失败');
  });
}


// 打开同步题库弹窗
const handleSync = () => {
  syncForm.subject = '';
  syncForm.model = '';
  syncVisible.value = true;
  nextTick(() => {
    syncFormRef.value?.resetFields();
  });
}

// 提交同步题库
const submitSync = () => {
  if (!syncForm.subject || !syncForm.model) {
    ElMessage.error('请选择科目类型和驾照类型');
    return;
  }
  ElMessageBox.confirm(
      `是否确认同步【${syncForm.subject === '1' ? '科目一' : '科目四'}】-${syncForm.model.toUpperCase()}驾照的题库？`,
      '同步确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info',
      }
  ).then(async () => {
    try {
      await syncQuestionBank({
        subject: Number(syncForm.subject),
        model: syncForm.model
      });
      ElMessage.success('题库同步成功');
      syncVisible.value = false;
      getQuestionList();
    } catch (err) {
      console.error('同步题库失败：', err);
      ElMessage.error('同步题库失败，请检查网络或API配置');
    }
  }).catch(() => {
    ElMessage.info('已取消同步');
  });
}

// 提交生成试卷
const submitGeneratePaper = () => {
  if (!paperForm.subject || !paperForm.model) {
    ElMessage.error('请选择科目类型和驾照类型');
    return;
  }
  generateExamPaper({
    subject: Number(paperForm.subject),
    model: paperForm.model
  }).then(res => {
    console.log('生成试卷返回：', res);
    if (res.data && res.data.length > 0) {
      ElMessage.success(`生成成功，共${res.data.length}道题目`);
      // 可在这里处理试卷数据，比如跳转试卷预览页
      paperVisible.value = false;
    } else {
      ElMessage.warning('暂无足够题目生成试卷，请先同步题库');
    }
  }).catch(err => {
    console.error('生成试卷失败：', err);
    ElMessage.error('生成试卷失败');
  });
}

// 初始化加载题目列表
getQuestionList();
</script>

<style scoped>
/* 表单样式优化 */
.demo-form-inline .el-input {
  --el-input-width: 220px;
}

/* 按钮行样式 */
.button-row {
  margin-bottom: 16px;
}

/* 分页样式 */
.pagination {
  margin-top: 15px;
  background-color: white;
  display: flex;
  justify-content: flex-start;
  padding: 10px;
}

/* 弹窗样式优化 */
.dialog-footer {
  text-align: right;
}

/* 表格样式优化 */
:deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 600;
}

/* 选项列样式 */
:deep(.el-table .cell) {
  line-height: 1.5;
}

/* 强制表格行高 */
:deep(.el-table__row) {
  height: 80px !important;
  line-height: 80px !important;
}

</style>