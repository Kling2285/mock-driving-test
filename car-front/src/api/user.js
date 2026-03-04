import request from "@/utils/request.js";

// 1. 查询全部用户
export function listuser(params) {
    // 注意：后端接口路径是 /user/list，需和后端保持一致（你原代码写的/list01是错误的）
    return request.get('/user/list', {
        params: params
    })
}

// 2. 条件查询用户
export function listUserByCondition(params) {
    return request.get('/user/listByCondition', {
        params: params
    })
}

// 3. 单个查询用户（根据ID）
export function selectone(userId) {
    return request.get(`/user/${userId}`);
}

// 4. 添加用户
export function adduser(data) {
    return request.post('/user', data);
}

// 5. 修改用户
export function updateuser(data) {
    return request.put('/user', data);
}

// 6. 删除用户
export function deleteuser(userId) {
    return request.delete(`/user/single/${userId}`);
}

export function exportUser(params) {
    return request({
        url: '/user/export',
        method: 'get',
        params: params,
        responseType: 'blob',
        headers: {
            'Accept': 'application/vnd.ms-excel, application/json'
        }
    })
}

// 8. 导入用户数据【修正版】
export function importUser(file) {
    const formData = new FormData();
    formData.append('file', file); // 和后端@RequestParam("file")对应
    return request({
        url: '/user/import',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 9. 分页查询用户（支持条件+分页）
export function pageUser(params) {
    return request.get('/user/page', {
        params: params
    })
}

//10.批量删除
export function batchDeleteUser(userIdList) {
    return request({
        url: '/user/batch',
        method: 'delete',
        data: userIdList // 传递JSON数组格式的ID列表
    })
}

export function updatePassword(data) {
    return request({
        url: '/user/updatePassword',
        method: 'post',
        data: data
    });
}