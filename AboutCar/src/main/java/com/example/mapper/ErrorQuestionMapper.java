package com.example.mapper;

import com.example.entity.ErrorQuestion;
import com.example.vo.ErrorQuestionWithQuestionVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ErrorQuestionMapper {

    /**
     * 新增错题记录
     */
    void insert(ErrorQuestion errorQuestion);

    /**
     * 根据ID查询单条错题记录
     */
    ErrorQuestion findById(@Param("id") Long id);

    /**
     * 根据ID更新错题记录
     */
    int updateById(ErrorQuestion errorQuestion);

    /**
     * 根据ID删除单条错题记录
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据用户ID查询该用户所有错题记录
     */
    List<ErrorQuestion> findByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID+科目+驾照类型查询错题记录
     */
    List<ErrorQuestion> findByUserIdSubjectAndModel(
            @Param("userId") Long userId,
            @Param("subject") Byte subject,
            @Param("model") String model
    );

    /**
     * 根据用户ID+科目统计错题总数
     */
    Integer countByUserIdAndSubject(
            @Param("userId") Long userId,
            @Param("subject") Byte subject
    );

    /**
     * 联表查询：根据用户ID获取「错题+题目详情」列表
     * @param userId 用户ID
     * @return 错题+题目详情VO列表
     */
    List<ErrorQuestionWithQuestionVO> findErrorQuestionWithQuestionByUserId(@Param("userId") Long userId);

    /**
     * 可选：联表查询：根据用户ID+科目+驾照类型获取「错题+题目详情」列表（按需新增）
     */
    List<ErrorQuestionWithQuestionVO> findErrorQuestionWithQuestionByUserIdSubjectAndModel(
            @Param("userId") Long userId,
            @Param("subject") Byte subject,
            @Param("model") String model
    );

    /**
     * 联表查询：根据「用户ID+科目+驾照类型+考试记录ID」获取该次考试的错题+题目详情
     * 核心：通过 examRecordId 过滤，只返回该次考试的错题
     */
    List<ErrorQuestionWithQuestionVO> findErrorQuestionWithQuestionByExamRecordId(
            @Param("userId") Long userId,
            @Param("subject") Byte subject,
            @Param("model") String model,
            @Param("examRecordId") Long examRecordId
    );
}