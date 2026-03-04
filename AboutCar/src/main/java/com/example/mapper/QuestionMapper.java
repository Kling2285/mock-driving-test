package com.example.mapper;

import com.example.entity.Question;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 驾照题目Mapper（对应question表，含题库同步核心方法）
 * 模仿AdminMapper风格编写，规范方法命名与参数定义
 */
public interface QuestionMapper {
    /**
     * 按科目+驾照类型删除本地旧题库（同步前清空数据，避免重复）
     * @param subject 科目类型（1=科目一，4=科目四）
     * @param model 驾照类型（c1/c2/a1/a2/b1/b2）
     */
    void deleteBySubjectAndModel(
            @Param("subject") Integer subject,
            @Param("model") String model
    );

    /**
     * 批量插入新题库数据（同步核心方法，批量导入API获取的题目）
     * @param questionList 题目实体列表
     */
    void batchInsert(List<Question> questionList);

    /**
     * 按科目+驾照类型+题型查询题目（组卷时使用，支持数量限制）
     * @param subject 科目类型
     * @param model 驾照类型
     * @param questionType 题型（1=判断题，2=单选题，3=多选题）
     * @param limit 查询数量上限
     * @return 符合条件的题目列表
     */
    List<Question> selectBySubjectModelAndType(
            @Param("subject") Integer subject,
            @Param("model") String model,
            @Param("questionType") Integer questionType,
            @Param("limit") Integer limit
    );

    /**
     * 查询所有题目（模仿AdminMapper.findAll()，全量查询）
     * @return 所有驾照题目列表
     */
    List<Question> findAll();

    /**
     * 根据题目ID查询单条题目（模仿AdminMapper.findById()）
     * @param id 题目ID（API返回的原始ID）
     * @return 题目实体
     */
    Question findById(@Param("id") Long id);

    /**
     * 根据科目+驾照类型查询题目（补充查询方法，适配业务需求）
     * @param subject 科目类型
     * @param model 驾照类型
     * @return 对应科目+驾照类型的题目列表
     */
    List<Question> findBySubjectAndModel(
            @Param("subject") Integer subject,
            @Param("model") String model
    );

    /**
     * 根据题目ID更新题目（模仿AdminMapper.updateById()，适配数据修正场景）
     * @param question 题目实体（含更新字段与题目ID）
     * @return 受影响行数
     */
    int updateById(Question question);

    /**
     * 根据题目ID删除单条题目（模仿AdminMapper.deleteById()，精准删除）
     * @param id 题目ID
     * @return 受影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据科目+驾照类型+API编号查询唯一题目
     * 适配api_id非唯一场景，通过三者组合定位单条题目
     * @param subject 科目类型（1=科目一，4=科目四）
     * @param model 驾照类型（c1/c2/a1等）
     * @param apiId 题目API编号（关联error_question表的api_id）
     * @return 唯一题目实体（无匹配数据返回null）
     */
    Question findBySubjectModelAndApiId(
            @Param("subject") Integer subject,
            @Param("model") String model,
            @Param("apiId") String apiId
    );
}