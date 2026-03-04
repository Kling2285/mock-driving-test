import request from "@/utils/request.js";

// 1. 新增考试记录
export function addExamRecord(data) {
    return request.post('/examRecord', data);
}

// 2. 根据ID查询单条考试记录
export function selectOneExamRecord(examRecordId) {
    return request.get(`/examRecord/${examRecordId}`);
}

// 3. 修改考试记录
export function updateExamRecord(data) {
    return request.put('/examRecord', data);
}

// 4. 根据ID删除考试记录
export function deleteExamRecord(examRecordId) {
    return request.delete(`/examRecord/${examRecordId}`);
}

// 5. 根据用户ID查询该用户所有考试记录
export function listExamRecordByUserId(params) {
    return request.get('/examRecord/listByUserId', {
        params: params
    })
}

// 6. 根据用户ID+科目+驾照类型查询考试记录
export function listExamRecordByUserIdSubjectAndModel(params) {
    return request.get('/examRecord/listByUserIdSubjectAndModel', {
        params: params
    })
}

// 7. 根据用户ID+科目统计考试总次数
export function countExamRecordTotal(params) {
    return request.get('/examRecord/countTotal', {
        params: params
    })
}

// 8. 根据用户ID+科目统计最高分
export function countExamRecordMaxScore(params) {
    return request.get('/examRecord/countMaxScore', {
        params: params
    })
}

// 9. 根据用户ID+科目统计通过次数
export function countExamRecordPassed(params) {
    return request.get('/examRecord/countPassed', {
        params: params
    })
}

// 10. 根据用户ID+科目计算通过率
export function calculateExamRecordPassRate(params) {
    return request.get('/examRecord/calculatePassRate', {
        params: params
    })
}

// 11. 根据用户ID查询最新一条考试记录
export function selectLatestExamRecord(params) {
    return request.get('/examRecord/latest', {
        params: params
    })
}

// 12. 多条件组合查询考试记录
export function listExamRecordByCondition(params) {
    return request.get('/examRecord/listByCondition', {
        params: params
    })
}

// 13. 分页查询考试记录
export function pageExamRecord(params) {
    return request.get('/examRecord/page', {
        params: params
    })
}