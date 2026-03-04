package com.example.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.entity.Question;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聚合数据驾照题库API工具类（适配纯SSM项目，可@Component注入读取配置）
 */
@Component // 标注为Spring组件，可注入配置文件中的appKey
public class JuheDrivingApiUtil {
    // 从SSM配置文件（db.properties/ssm.properties）中读取appKey，避免硬编码
    @Value("${juhe.driving.appKey}")
    private String appKey; // 不再硬编码，通过@Value注入

    private static final String QUERY_URL = "http://v.juhe.cn/jztk/query";
    private static final String ANSWER_URL = "http://v.juhe.cn/jztk/answers";
    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();

    /**
     * 获取驾照题库原始JSON数据
     * @param subject 科目（1=科目一，4=科目四）
     * @param model 驾照类型（c1/c2等）
     * @param testType 测试类型（rand=随机100题，order=全部题目）
     * @return JSONObject
     */
    public JSONObject getDrivingQuestionBank(int subject, String model, String testType) {
        Map<String, String> params = new HashMap<>();
        params.put("key", appKey); // 使用注入的appKey
        params.put("subject", String.valueOf(subject));
        params.put("model", model);
        if (testType != null && !testType.isEmpty()) {
            params.put("testType", testType);
        }

        // 构建表单请求体
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody requestBody = formBuilder.build();

        // 构建POST请求
        Request request = new Request.Builder()
                .url(QUERY_URL)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseJson = response.body().string();
                return JSON.parseObject(responseJson);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("调用聚合数据题库接口失败：" + e.getMessage());
        }
        return null;
    }

    /**
     * 获取答案映射原始JSON数据
     * @return JSONObject
     */
    public JSONObject getDrivingAnswerMap() {
        FormBody.Builder formBuilder = new FormBody.Builder();
        formBuilder.add("key", appKey); // 使用注入的appKey
        RequestBody requestBody = formBuilder.build();

        Request request = new Request.Builder()
                .url(ANSWER_URL)
                .post(requestBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        try (Response response = OK_HTTP_CLIENT.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseJson = response.body().string();
                return JSON.parseObject(responseJson);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("调用聚合数据答案映射接口失败：" + e.getMessage());
        }
        return null;
    }

    /**
     * 新增：直接返回Question实体类列表（适配SSM业务层，无需手动解析）
     * @param subject 科目
     * @param model 驾照类型
     * @param testType 测试类型
     * @return List<Question>
     */
    public List<Question> getQuestionList(int subject, String model, String testType) {
        JSONObject jsonObject = this.getDrivingQuestionBank(subject, model, testType);
        if (jsonObject == null) {
            throw new RuntimeException("未获取到题库数据");
        }

        // 校验API响应状态
        int errorCode = jsonObject.getInteger("error_code");
        if (errorCode != 0) {
            String reason = jsonObject.getString("reason");
            throw new RuntimeException("API返回错误：" + reason + "（错误码：" + errorCode + "）");
        }

        // 解析为Question列表并补充本地字段
        JSONArray questionArray = jsonObject.getJSONArray("result");
        List<Question> questionList = questionArray.toJavaList(Question.class);
        for (Question question : questionList) {
            // 1. 先赋值业务字段
            question.setSubject(subject);
            question.setModel(model);
            question.setQuestionType(this.judgeQuestionType(question));
            // 2. 再处理ID（核心：最后执行，避免被覆盖；加非空判断）
            if (question.getId() != null) {
                question.setApiId(question.getId()); // 存储API原始ID
            }
            question.setId(null); // 清空自增ID，让数据库自动生成
        }

        return questionList;
    }

    /**
     * 辅助方法：自动判断题型（1=判断题，2=单选题，3=多选题）
     */
    private Integer judgeQuestionType(Question question) {
        if ((question.getItem3() == null || question.getItem3().trim().isEmpty())
                && (question.getItem4() == null || question.getItem4().trim().isEmpty())) {
            return 1; // 判断题
        } else if (question.getSubject() == 4 && Integer.parseInt(question.getAnswer()) >= 7) {
            return 3; // 多选题
        } else {
            return 2; // 单选题
        }
    }
}