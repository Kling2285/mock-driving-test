import request from "@/utils/request.js";

// 1. 查询全部管理员（无分页）
export function listAdmin(params) {
    return request.get('/admin/list', {
        params: params
    })
}

// 2. 条件查询管理员
export function listAdminByCondition(params) {
    return request.get('/admin/listByCondition', {
        params: params
    })
}

// 3. 单个查询管理员（根据ID）
export function selectOneAdmin(adminId) {
    return request.get(`/admin/${adminId}`);
}

// 4. 添加管理员
export function addAdmin(data) {
    return request.post('/admin', data);
}

// 5. 修改管理员
export function updateAdmin(data) {
    return request.put('/admin', data);
}

// 6. 删除管理员
export function deleteAdmin(adminId) {
    return request.delete(`/admin/${adminId}`);
}

// 7. 分页查询管理员（支持条件+分页）
export function pageAdmin(params) {
    return request.get('/admin/page', {
        params: params
    })
}

// 8. 导出管理员数据（带条件筛选，指定blob响应类型）
export function exportAdmin(params) {
    return request({
        url: '/admin/export',
        method: 'get',
        params: params,
        responseType: 'blob',
        headers: {
            'Accept': 'application/vnd.ms-excel, application/json'
        }
    })
}

// 9. 导入管理员数据（适配文件上传）
export function importAdmin(file) {
    const formData = new FormData();
    formData.append('file', file); // 和后端@RequestParam("file")对应
    return request({
        url: '/admin/import',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}