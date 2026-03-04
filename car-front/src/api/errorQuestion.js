import request from "@/utils/request.js";

// 1. 新增错题记录
export function addErrorQuestion(data) {
    return request.post('/errorQuestion', data);
}

// 2. 根据ID查询单条错题记录
export function selectOneErrorQuestion(errorQuestionId) {
    return request.get(`/errorQuestion/${errorQuestionId}`);
}

// 3. 根据ID更新错题记录
export function updateErrorQuestion(data) {
    return request.put('/errorQuestion', data);
}

// 4. 根据ID删除单条错题记录
export function deleteErrorQuestion(errorQuestionId) {
    return request.delete(`/errorQuestion/${errorQuestionId}`);
}

// 5. 根据用户ID查询该用户所有错题记录
export function listErrorQuestionByUserId(params) {
    return request.get('/errorQuestion/listByUserId', {
        params: params
    })
}

// 6. 根据用户ID+科目+驾照类型查询错题记录
export function listErrorQuestionByUserIdSubjectAndModel(params) {
    return request.get('/errorQuestion/listByUserIdSubjectAndModel', {
        params: params
    })
}

// 7. 根据用户ID+科目统计错题总数
export function countErrorQuestionByUserIdAndSubject(params) {
    return request.get('/errorQuestion/countByUserIdAndSubject', {
        params: params
    })
}

//，实现某次考试错题查询接口
export function listWithQuestionByExamRecord(params) {
    return request.get('/errorQuestion/listWithQuestionByExamRecord', {
        params: params
    })
}