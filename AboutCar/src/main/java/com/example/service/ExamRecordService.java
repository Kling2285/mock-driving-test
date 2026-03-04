package com.example.service;

import com.example.common.PageResult;
import com.example.entity.ExamRecord;

import java.util.List;

public interface ExamRecordService {

    /**
     * 新增考试记录
     */
    void insert(ExamRecord examRecord);

    /**
     * 根据ID查询单条考试记录
     */
    ExamRecord findById(Long id);

    /**
     * 根据ID更新考试记录
     */
    void updateById(ExamRecord examRecord);

    /**
     * 根据ID删除考试记录
     */
    void deleteById(Long id);

    /**
     * 根据用户ID查询该用户所有考试记录
     */
    List<ExamRecord> findByUserId(Long userId);

    /**
     * 根据用户ID+科目+驾照类型查询考试记录
     */
    List<ExamRecord> findByUserIdSubjectAndModel(Long userId, Byte subject, String model);

    /**
     * 根据用户ID+科目统计考试总次数
     */
    Integer countTotalByUserIdAndSubject(Long userId, Byte subject);

    /**
     * 根据用户ID+科目统计最高分
     */
    Integer countMaxScoreByUserIdAndSubject(Long userId, Byte subject);

    /**
     * 根据用户ID+科目统计通过次数
     */
    Integer countPassedByUserIdAndSubject(Long userId, Byte subject);

    /**
     * 根据用户ID+科目计算通过率
     */
    Double calculatePassRateByUserIdAndSubject(Long userId, Byte subject);

    /**
     * 根据用户ID查询最新一条考试记录
     */
    ExamRecord findLatestByUserId(Long userId);

    /**
     * 批量查询考试记录（多条件组合）
     */
    List<ExamRecord> findListByCondition(Long userId, Byte subject, String model, Byte isPass);

    /**
     * 分页查询考试记录
     */
    PageResult<ExamRecord> findByPage(Long userId, Byte subject, String model, Byte isPass, Integer pageNum, Integer pageSize);
}