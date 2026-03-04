import request from "@/utils/request.js";

// 1. 查询所有题目（无分页）
export function listQuestion() {
    return request.get('/question/list');
}

// 2. 按科目+驾照类型+题型条件查询题目
export function listQuestionByCondition(params) {
    return request.get('/question/listByCondition', {
        params: params // params = {subject: 1, model: 'c1', questionType: 2}
    });
}

// 3. 新增单个题目
export function addQuestion(data) {
    return request.post('/question', data);
}

// 4. 根据题目ID查询单个题目（路径传参）
export function selectOneQuestion(questionId) {
    return request.get(`/question/${questionId}`);
}

// 5. 更新题目
export function updateQuestion(data) {
    return request.put('/question', data);
}


// 7. 题目分页查询（支持条件+分页）
export function pageQuestion(params) {
    return request.get('/question/page', {
        params: params // params = {subject:1, model:'c1', questionType:2, pageNum:1, pageSize:10}
    });
}

// 8. 按科目+驾照类型查询题目（必填subject和model）
export function listQuestionBySubjectAndModel(params) {
    return request.get('/question/listBySubjectAndModel', {
        params: params // params = {subject: 1, model: 'c1'}
    });
}

// 9. 题库同步接口（核心功能：从API同步到本地数据库）
export function syncQuestionBank(params) {
    return request.post('/question/sync', null, {
        params: params // params = {subject: 1, model: 'c1'}
    });
}

// 10. 生成模拟试卷接口
export function generateExamPaper(params) {
    return request.get('/question/generateExamPaper', {
        params: params // params = {subject: 1, model: 'c1'}
    });
}

// 按指定题型生成100道题模拟试卷接口（与generateExamPaper风格统一）
export function generateSpecialTypeExamPaper(params) {
    return request.get('/question/generateSpecialTypeExamPaper', {
        params: params // params = {subject: 1, model: 'c1', questionType: 1}
    });
}

// 11. 导出题目Excel（带条件筛选，指定blob响应类型）
export function exportQuestion(params) {
    return request({
        url: '/question/export',
        method: 'get',
        params: params, // params = {subject:1, model:'c1', questionType:2}
        responseType: 'blob', // 关键：指定响应类型为blob（文件流）
        headers: {
            'Accept': 'application/vnd.ms-excel, application/json'
        }
    });
}

