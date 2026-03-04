package com.example.service;

import com.example.common.PageResult;
import com.example.entity.Question;

import java.util.List;

/**
 * 驾照题目服务接口（模仿AdminService风格，规范业务方法定义）
 */
public interface QuestionService {

    /**
     * 查询所有题目
     */
    List<Question> findAll();

    /**
     * 按科目+驾照类型+题型查询题目
     * @param subject 科目类型（1=科目一，4=科目四）
     * @param model 驾照类型（c1/c2/a1/a2）
     * @param questionType 题型（1=判断题，2=单选题，3=多选题）
     * @return 符合条件的题目列表
     */
    List<Question> findListByCondition(Integer subject, String model, Integer questionType);

    /**
     * 根据题目ID查询单个题目
     * @param id 题目ID（数据库自增主键）
     */
    Question findById(Long id);

    /**
     * 新增单个题目
     * @param question 题目实体
     */
    void insert(Question question);

    /**
     * 更新题目
     * @param question 题目实体（必须包含id）
     */
    void updateById(Question question);

    /**
     * 根据题目ID删除单个题目
     * @param id 题目ID
     */
    void deleteById(Long id);

    /**
     * 题目分页查询
     * @param subject 科目类型（可选，模糊查询可传null）
     * @param model 驾照类型（可选，模糊查询可传null）
     * @param questionType 题型（可选，模糊查询可传null）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果对象
     */
    PageResult<Question> findQuestionByPage(Integer subject, String model, Integer questionType, Integer pageNum, Integer pageSize);

    /**
     * 按科目+驾照类型查询题目
     * @param subject 科目类型
     * @param model 驾照类型
     * @return 对应科目+驾照类型的题目列表
     */
    List<Question> findBySubjectAndModel(Integer subject, String model);

    /**
     * 题库同步核心方法：将API数据同步到本地数据库
     * @param subject 科目类型
     * @param model 驾照类型
     */
    void syncQuestionBankFromApi(Integer subject, String model);

    /**
     * 按考试规则生成模拟试卷
     * @param subject 科目类型
     * @param model 驾照类型
     * @return 符合考试题型数量的题目列表
     */
    List<Question> generateExamPaper(Integer subject, String model);


    /**
     * 按指定题型生成模拟试卷（100道题，仅包含单一指定题型）
     * @param subject 科目类型（1=科目一，4=科目四）
     * @param model 驾照类型（c1/c2/a1/a2）
     * @param questionType 题型（1=判断题，2=单选题，3=多选题）
     * @return 100道对应题型的题目列表
     */
    List<Question> generateSpecialTypeExamPaper(Integer subject, String model, Integer questionType);

    /**
     * 根据科目+驾照类型+API编号查询唯一题目
     * 适配api_id非唯一场景，用于关联错题表查询题目详情
     * @param subject 科目类型（1=科目一，4=科目四）
     * @param model 驾照类型（c1/c2/a1等）
     * @param apiId 题目API编号
     * @return 唯一题目实体（无匹配数据返回null）
     */
    Question findBySubjectModelAndApiId(Integer subject, String model, String apiId);


}