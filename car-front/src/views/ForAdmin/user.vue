<template>
  <div>
    <div class="box">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="用户ID">
          <el-input v-model="queryParams.userId" placeholder="请输入用户ID" clearable type="number" />
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="用户昵称">
          <el-input v-model="queryParams.nickname" placeholder="请输入用户昵称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button color="#3869f4" plain @click="search">
            <span style="margin-left: 4px;">查询</span>
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button type="info" plain @click="list00">
            <span style="margin-left: 4px;">重置</span>
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮区：给批量删除添加加载状态 -->
    <div class="box" style="display: flex; align-items: center; gap: 8px;">
      <div class="button-row" style="display: flex; align-items: center; gap: 8px;">
        <el-button type="primary" plain @click="handleAdd">
          <el-icon><Plus /></el-icon>添加
        </el-button>
        <el-button type="info" plain @click="handleExport">
          <el-icon><Upload /></el-icon>导出
        </el-button>
        <el-upload
            class="upload-demo"
            :show-file-list="false"
            :on-change="handleImportChange"
            :auto-upload="false"
            style="margin-left: 15px;"
        >
          <el-button type="warning" plain>
            <el-icon><Download /></el-icon>导入
          </el-button>
        </el-upload>
      </div>
    </div>

    <!-- 表格：保留头像列，固定显示默认头像 -->
    <div>
      <el-table :data="data.tableData" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" prop="userId" width="100" />
        <el-table-column prop="userId" label="用户ID" width="180" />
        <el-table-column prop="username" label="用户名" width="180" />
        <el-table-column prop="nickname" label="用户昵称" width="180" />
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">
              <el-icon><EditPen /></el-icon>编辑
            </el-button>
            <el-button
                size="small"
                type="danger"
                @click="handleDelete(scope.row)"
                :loading="deleteLoading"
            >
              <el-icon><CloseBold /></el-icon>删除
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

    <el-dialog v-model="visible" :title="title" width="700">
      <el-form :model="form" :label-width="85" :rules="rules" ref="form1">
        <!-- 隐藏 userType 表单项，确保提交时携带 -->
        <el-form-item label="用户类型" prop="userType" style="display: none;">
          <el-input v-model="form.userType" disabled />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" autocomplete="off"  />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" :disabled="!!form.userId" type="password" autocomplete="off" placeholder="新增必填，编辑不可改" />
        </el-form-item>
        <el-form-item label="用户昵称" prop="nickname">
          <el-input v-model="form.nickname" autocomplete="off" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" autocomplete="off" placeholder="请输入11位手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" autocomplete="off" placeholder="请输入有效邮箱" />
        </el-form-item>
        <el-form-item label="创建时间" v-if="form.createTime">
          <el-input v-model="form.createTime" readonly disabled autocomplete="off" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="visible = false">取消</el-button>
          <el-button
              type="primary"
              @click="submitForm"
              :loading="submitLoading"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, nextTick } from 'vue'
// 导入批量删除接口
import {
  deleteuser,
  adduser,
  selectone,
  updateuser,
  importUser,
  pageUser,
  batchDeleteUser
} from '@/api/user.js'
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Plus,CloseBold,Download,Upload,EditPen
} from '@element-plus/icons-vue'
import {download} from "@/utils/request.js";

// 批量删除选中的ID集合
const ids = ref([])
// 弹窗显隐
const visible = ref(false);
// 弹窗标题
const title = ref('添加用户');
// ========== 新增：防重复提交状态 ==========
const submitLoading = ref(false); // 表单提交加载状态
const deleteLoading = ref(false); // 删除操作加载状态

let selectedUserIds = [];

// 表单数据：彻底移除avatar字段
const form = reactive({
  userId: '',
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: '',
  createTime: '',
  updateTime: '',
  userType: ''
});

// 表单校验ref
const form1 = ref();

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  userId: '',
  username: '',
  nickname: ''
})

// 表单校验规则
const rules = reactive({
  username: [
    { required: true, message: '用户名不能为空', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度在2-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码至少6位字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '用户昵称不能为空', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '手机号不能为空', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的11位手机号', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '邮箱不能为空', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
});

// 表格数据
const data = reactive({
  total: 0,
  tableData: []
})


const getUserList = async () => {
  try {
    console.log('调用分页接口，参数：', queryParams);
    const res = await pageUser(queryParams);
    console.log('分页接口返回：', res);
    data.total = res.data?.total || 0;
    data.tableData = (res.data?.list || []).filter(item =>
        item.userId !== null && item.userId !== undefined && item.userId > 0
    );
  } catch (err) {
    console.error('获取用户列表失败：', err);
    ElMessage.error('获取用户列表失败');
  }
}

// 导出用户数据
const handleExport = () => {
  try {
    download('/user/export', {
      userId: queryParams.userId,
      username: queryParams.username,
      nickname: queryParams.nickname
    });
    ElMessage.success('导出请求已发送，文件即将下载');
  } catch (err) {
    console.error('导出失败：', err);
    ElMessage.error('导出失败，请检查参数或网络');
  }
};

// 导入用户数据
const handleImportChange = async (file) => {
  try {
    console.log('调用导入接口，文件：', file); // 调试：打印文件信息
    const fileName = file.name;
    if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xls')) {
      ElMessage.error('仅支持.xlsx/.xls格式的Excel文件');
      return;
    }
    const res = await importUser(file.raw);
    console.log('导入接口返回：', res); // 调试：打印返回结果
    ElMessage.success(`导入成功：共${res.data.total}条，成功${res.data.count}条`);
    getUserList(); // 导入后刷新列表
  } catch (err) {
    console.error('导入失败：', err); // 调试：打印错误
    ElMessage.error(err.response?.data?.msg || '导入失败');
  }
}

// 条件查询
const search = () => {
  queryParams.pageNum = 1;
  getUserList();
}

// 分页页码变更
const handleCurrentChange = (val) => {
  queryParams.pageNum = val;
  getUserList();
}

// 分页条数变更
const handleSizeChange = (val) => {
  queryParams.pageSize = val;
  queryParams.pageNum = 1;
  getUserList();
};

// 重置查询条件
const list00 = () => {
  queryParams.pageNum = 1;
  queryParams.pageSize = 10;
  queryParams.userId = '';
  queryParams.username = '';
  queryParams.nickname = '';
  getUserList();
}

const handleSelectionChange = (selections) => {
  selectedUserIds = selections.map(item => item.userId);
  console.log("选中的ID列表：", selectedUserIds); // 直接是 [6]，普通数组
};

const handleDelete = (row) => {
  let deleteIds = [];
  if (row) {
    deleteIds = [row.userId];
  } else {
    // 批量删除分支：先判断 ids.value 长度，再强制解包为普通数组
    if (ids.value.length === 0) {
      ElMessage.error("请选择要删除的数据");
      return;
    }
    // 关键修改：用 [...ids.value] 强制解包为普通数组，再赋值给 deleteIds
    deleteIds = [...ids.value];
    console.log("【批量删除-获取的deleteIds】", deleteIds); // 此时应该是 [6]
  }

  // 后续过滤逻辑不变，此时 deleteIds 是普通数组，过滤后不会为空
  const validDeleteIds = deleteIds
      .filter(id => id !== null && id !== undefined && id > 0)
      .map(id => Number(id));

  console.log("【批量删除-validDeleteIds】", validDeleteIds); // 此时应该是 [6]

  if (validDeleteIds.length === 0) {
    ElMessage.error("无有效用户ID，无法删除");
    return;
  }

  // 后续确认框和接口调用逻辑不变...
  ElMessageBox.confirm(
      `是否确认删除选中的${validDeleteIds.length}条用户数据？`,
      '删除确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    deleteLoading.value = true;
    try {
      const res = await batchDeleteUser(validDeleteIds);
      ElMessage.success(res.msg || `成功删除${validDeleteIds.length}条用户数据`);
      getUserList();
    } catch (err) {
      console.error("批量删除失败：", err);
      ElMessage.error(err.response?.data?.msg || "删除失败");
    } finally {
      deleteLoading.value = false;
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 新增用户
const handleAdd = () => {
  title.value = "添加用户";
  // 清空表单
  Object.keys(form).forEach(key => {
    form[key] = '';
  });
  // 强制赋值 userType 为 'user'
  form.userType = 'user';
  visible.value = true;
  nextTick(() => {
    form1.value?.resetFields();
  });
};

// 提交表单（新增/编辑）：添加防重复提交
const submitForm = () => {
  form1.value?.validate(async (isValid) => {
    if (isValid) {
      submitLoading.value = true; // 开启提交加载状态
      try {
        let res;
        if (form.userId) {
          // 编辑用户
          console.log('调用编辑接口，数据：', form); // 可查看 form 中是否包含 userType
          res = await updateuser(form);
          ElMessage.success("修改用户成功");
        } else {
          // 新增用户
          console.log('调用新增接口，数据：', form);
          res = await adduser(form);
          ElMessage.success("添加用户成功");
        }
        visible.value = false;
        getUserList();
      } catch (err) {
        console.error(form.userId ? '修改失败' : '添加失败', err);
        ElMessage.error(form.userId ? '修改失败' : '添加失败');
      } finally {
        submitLoading.value = false; // 关闭提交加载状态
      }
    }
  });
};

// 编辑用户
const handleEdit = (row) => {
  title.value = "编辑用户";
  console.log('调用单个查询接口，ID：', row.userId);
  selectone(row.userId).then(res => {
    console.log('单个查询接口返回：', res);
    if (res.data) {
      // 赋值表单
      form.userId = res.data.userId || '';
      form.username = res.data.username || '';
      form.password = res.data.password || '';
      form.nickname = res.data.nickname || '';
      form.phone = res.data.phone || '';
      form.email = res.data.email || '';
      form.createTime = res.data.createTime || '';
      form.updateTime = res.data.updateTime || '';
      // 赋值 userType，兜底为 'user'
      form.userType = res.data.userType || 'user';
      visible.value = true;
    } else {
      ElMessage.error('获取用户信息失败');
    }
  }).catch(err => {
    console.error('获取用户信息失败：', err);
    ElMessage.error('获取用户信息失败');
  });
}

// 初始化加载用户列表
getUserList();
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

/* 表格头像样式优化 */
:deep(.el-image) {
  border-radius: 50%;
}
</style>