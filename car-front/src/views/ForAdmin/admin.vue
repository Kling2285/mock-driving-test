<template>
  <div>
    <div class="box">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="管理员账号">
          <el-input v-model="queryParams.username" placeholder="请输入管理员账号" clearable />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="queryParams.realName" placeholder="请输入真实姓名" clearable />
        </el-form-item>
        <el-form-item label="角色ID">
          <el-input v-model="queryParams.roleId" placeholder="请输入角色ID" clearable type="number" />
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

    <!-- 操作按钮区 -->
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

    <div>
      <el-table :data="data.tableData" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" prop="adminId" width="100" />
        <el-table-column prop="adminId" label="管理员ID" width="180" />
        <el-table-column prop="username" label="管理员账号" width="180" />
        <el-table-column prop="realName" label="真实姓名" width="180" />
        <el-table-column prop="roleId" label="角色ID" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" @click="handleEdit(scope.row)">
              <el-icon><EditPen /></el-icon>编辑
            </el-button>
            <el-button
                size="small"
                type="danger"
                @click="handleDelete(scope.row)"
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
      <el-form :model="form" :label-width="100" :rules="rules" ref="form1">
        <el-form-item label="管理员账号" prop="username">
          <el-input v-model="form.username" autocomplete="off"  />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" :disabled="!!form.adminId" type="password" autocomplete="off" placeholder="新增必填，编辑不可改" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" autocomplete="off" />
        </el-form-item>
        <el-form-item label="角色ID" prop="roleId">
          <el-input
              v-model="form.roleId"
              autocomplete="off"
              type="number"
              placeholder="请输入角色ID"
              clearable
          />
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
          <el-button type="primary" @click="submitForm">
            确定
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, nextTick } from 'vue'
// 导入admin接口
import {
  deleteAdmin,
  addAdmin,
  selectOneAdmin,
  updateAdmin,
  exportAdmin,
  importAdmin,
  pageAdmin
} from '@/api/admin.js'
import { ElMessage, ElMessageBox } from "element-plus";
import {
  Plus, CloseBold, Download, Upload, EditPen
} from '@element-plus/icons-vue'
import { download } from "@/utils/request.js";

// 批量删除选中的ID集合
const ids = ref([])
// 弹窗显隐
const visible = ref(false);
// 弹窗标题
const title = ref('添加管理员');

// 表单数据：修复1 - 补充userType字段
const form = reactive({
  adminId: '',
  username: '',
  password: '',
  realName: '',
  roleId: null,
  phone: '',
  email: '',
  createTime: '',
  updateTime: '',
  userType: '' // 关键补充：恢复userType字段，解决字段缺失报错
});

// 表单校验ref
const form1 = ref();

// 查询参数：适配Admin条件查询字段，roleId初始值为null
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  username: '',
  realName: '',
  roleId: null
})

// 表单校验规则：完全修复roleId校验问题
const rules = reactive({
  username: [
    { required: true, message: '管理员账号不能为空', trigger: 'blur' },
    { min: 2, max: 20, message: '管理员账号长度在2-20个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '密码不能为空', trigger: 'blur' },
    { min: 6, message: '密码至少6位字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '真实姓名不能为空', trigger: 'blur' }
  ],
  roleId: [
    { required: true, message: '角色ID不能为空', trigger: 'blur' },
    {
      type: 'number',
      message: '角色ID必须为数字',
      trigger: 'blur',
      transform: (value) => {
        // 强制转换为数字，兼容bigint（Long）类型
        const num = Number(value);
        return isNaN(num) ? null : num;
      }
    },
    {
      validator: (rule, value, callback) => {
        // 自定义校验：确保是正整数（兼容Long类型，避免小数/负数问题）
        if (value !== null && value !== undefined && value !== '') {
          const num = Number(value);
          if (num <= 0 || !Number.isInteger(num)) {
            callback(new Error('角色ID必须为正整数'));
          } else {
            callback();
          }
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
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

// 获取管理员列表（核心：分页查询接口调用）
const getAdminList = async () => {
  try {
    console.log('调用管理员分页接口，参数：', queryParams);
    const res = await pageAdmin(queryParams);
    console.log('管理员分页接口返回：', res);
    data.total = res.data?.total || 0;
    data.tableData = res.data?.list || [];
  } catch (err) {
    console.error('获取管理员列表失败：', err);
    ElMessage.error('获取管理员列表失败');
  }
}

// 导出管理员数据
const handleExport = () => {
  try {
    download('/admin/export', {
      username: queryParams.username,
      realName: queryParams.realName,
      roleId: queryParams.roleId
    });
    ElMessage.success('导出请求已发送，文件即将下载');
  } catch (err) {
    console.error('导出管理员失败：', err);
    ElMessage.error('导出失败，请检查参数或网络');
  }
};

// 导入管理员数据
const handleImportChange = async (file) => {
  try {
    console.log('调用管理员导入接口，文件：', file);
    // 前置校验：文件格式
    const fileName = file.name;
    if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xls')) {
      ElMessage.error('仅支持.xlsx/.xls格式的Excel文件');
      return;
    }
    // 调用导入接口
    const res = await importAdmin(file.raw);
    console.log('管理员导入接口返回：', res);
    ElMessage.success(`导入成功：共${res.data.total}条，成功${res.data.count}条`);
    getAdminList(); // 导入后刷新列表
  } catch (err) {
    console.error('导入管理员失败：', err);
    ElMessage.error(err.response?.data?.msg || '导入失败');
  }
}

// 条件查询
const search = () => {
  queryParams.pageNum = 1;
  getAdminList();
}

// 分页页码变更
const handleCurrentChange = (val) => {
  queryParams.pageNum = val;
  getAdminList();
}

// 分页条数变更
const handleSizeChange = (val) => {
  queryParams.pageSize = val;
  queryParams.pageNum = 1;
  getAdminList();
};

// 重置查询条件
const list00 = () => {
  queryParams.pageNum = 1;
  queryParams.pageSize = 10;
  queryParams.username = '';
  queryParams.realName = '';
  queryParams.roleId = null;
  getAdminList();
}

// 表格选中事件
const handleSelectionChange = (selections) => {
  ids.value = selections.map(item => item.adminId);
}

// 删除管理员
const handleDelete = (row) => {
  let deleteIds = [];
  if (row) {
    deleteIds = [row.adminId];
  } else {
    if (ids.value.length === 0) {
      ElMessage.error("请选择要删除的管理员数据");
      return;
    }
    deleteIds = ids.value;
  }

  ElMessageBox.confirm(
      `是否确认删除选中的${deleteIds.length}条管理员数据？`,
      '删除确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    let successCount = 0;
    for (const id of deleteIds) {
      try {
        console.log('调用删除管理员接口，ID：', id);
        await deleteAdmin(id);
        successCount++;
      } catch (err) {
        console.error(`删除ID${id}的管理员失败：`, err);
        ElMessage.error(`删除ID为${id}的管理员失败`);
      }
    }
    if (successCount > 0) {
      ElMessage.success(`成功删除${successCount}条管理员数据`);
      getAdminList();
    }
  }).catch(() => {
    ElMessage.info('已取消删除');
  });
};

// 新增管理员：修复2 - 清空表单后赋值userType为admin
const handleAdd = () => {
  title.value = "添加管理员";
  // 针对性清空，避免统一赋值覆盖roleId类型
  form.adminId = '';
  form.username = '';
  form.password = '';
  form.realName = '';
  form.roleId = null;
  form.phone = '';
  form.email = '';
  form.createTime = '';
  form.updateTime = '';
  // 关键赋值：新增管理员时强制设置userType为admin
  form.userType = 'admin';
  visible.value = true;
  // 重置表单校验
  nextTick(() => {
    form1.value?.resetFields();
  });
}

// 提交表单（新增/编辑管理员）
const submitForm = () => {
  form1.value?.validate(async (isValid) => {
    if (isValid) {
      try {
        let res;
        if (form.adminId) {
          // 编辑管理员
          console.log('调用编辑管理员接口，数据：', form);
          res = await updateAdmin(form);
          ElMessage.success("修改管理员成功");
        } else {
          // 新增管理员（已自动携带userType: admin）
          console.log('调用新增管理员接口，数据：', form);
          res = await addAdmin(form);
          ElMessage.success("添加管理员成功");
        }
        visible.value = false;
        getAdminList();
      } catch (err) {
        console.error(form.adminId ? '修改管理员失败' : '添加管理员失败', err);
        ElMessage.error(form.adminId ? '修改管理员失败' : '添加管理员失败');
      }
    }
  });
};

// 编辑管理员：修复roleId赋值逻辑，为空时赋值null，并携带userType
const handleEdit = (row) => {
  title.value = "编辑管理员";
  console.log('调用单个查询管理员接口，ID：', row.adminId);
  selectOneAdmin(row.adminId).then(res => {
    console.log('单个查询管理员接口返回：', res);
    if (res.data) {
      form.adminId = res.data.adminId || '';
      form.username = res.data.username || '';
      form.password = res.data.password || '';
      form.realName = res.data.realName || '';
      form.roleId = res.data.roleId || null; // 关键修复：赋值null而非''
      form.phone = res.data.phone || '';
      form.email = res.data.email || '';
      form.createTime = res.data.createTime || '';
      form.updateTime = res.data.updateTime || '';
      // 编辑时赋值userType，保持原有admin类型不变
      form.userType = res.data.userType || 'admin';
      visible.value = true;
    } else {
      ElMessage.error('获取管理员信息失败');
    }
  }).catch(err => {
    console.error('获取管理员信息失败：', err);
    ElMessage.error('获取管理员信息失败');
  });
}

// 初始化加载管理员列表
getAdminList();
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
</style>