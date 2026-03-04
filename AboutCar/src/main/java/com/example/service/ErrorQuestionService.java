package com.example.service;

import com.example.entity.ErrorQuestion;
import com.example.vo.ErrorQuestionWithQuestionVO;

import java.util.List;

public interface ErrorQuestionService {

    /**
     * 新增错题记录
     */
    void insert(ErrorQuestion errorQuestion);

    /**
     * 根据ID查询单条错题记录
     */
    ErrorQuestion findById(Long id);

    /**
     * 根据ID更新错题记录
     */
    void updateById(ErrorQuestion errorQuestion);

    /**
     * 根据ID删除单条错题记录
     */
    void deleteById(Long id);

    /**
     * 根据用户ID查询该用户所有错题记录
     */
    List<ErrorQuestion> findByUserId(Long userId);

    /**
     * 根据用户ID+科目+驾照类型查询错题记录
     */
    List<ErrorQuestion> findByUserIdSubjectAndModel(Long userId, Byte subject, String model);

    /**
     * 根据用户ID+科目统计错题总数
     */
    Integer countByUserIdAndSubject(Long userId, Byte subject);


    /**
     * 联表查询：根据用户ID获取错题+题目详情列表
     */
    List<ErrorQuestionWithQuestionVO> getErrorQuestionWithQuestionByUserId(Long userId);

    /**
     * 可选：联表查询：用户ID+科目+驾照类型
     */
    List<ErrorQuestionWithQuestionVO> getErrorQuestionWithQuestionByUserIdSubjectAndModel(Long userId, Byte subject, String model);

    /**
     * 获取某次考试的错题+题目详情（用户ID+科目+驾照类型+考试记录ID）
     */
    List<ErrorQuestionWithQuestionVO> getErrorQuestionWithQuestionByExamRecordId(
            Long userId,
            Byte subject,
            String model,
            Long examRecordId
    );
}