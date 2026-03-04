package com.example.mapper;

import com.example.entity.ExamRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ExamRecordMapper {

    /**
     * 新增考试记录
     */
    void insert(ExamRecord examRecord);

    /**
     * 根据ID查询单条考试记录
     */
    ExamRecord findById(@Param("id") Long id);

    /**
     * 根据ID更新考试记录
     */
    int updateById(ExamRecord examRecord);

    /**
     * 根据ID删除考试记录
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据用户ID查询该用户所有考试记录
     */
    List<ExamRecord> findByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID+科目+驾照类型查询考试记录
     */
    List<ExamRecord> findByUserIdSubjectAndModel(
            @Param("userId") Long userId,
            @Param("subject") Byte subject,
            @Param("model") String model
    );

    /**
     * 根据用户ID+科目统计考试总次数
     */
    Integer countTotalByUserIdAndSubject(
            @Param("userId") Long userId,
            @Param("subject") Byte subject
    );

    /**
     * 根据用户ID+科目统计最高分
     */
    Integer countMaxScoreByUserIdAndSubject(
            @Param("userId") Long userId,
            @Param("subject") Byte subject
    );

    /**
     * 根据用户ID+科目统计通过次数
     */
    Integer countPassedByUserIdAndSubject(
            @Param("userId") Long userId,
            @Param("subject") Byte subject
    );

    /**
     * 根据用户ID查询最新一条考试记录
     */
    ExamRecord findLatestByUserId(@Param("userId") Long userId);

    /**
     * 批量查询考试记录（多条件组合）
     */
    List<ExamRecord> findListByCondition(
            @Param("userId") Long userId,
            @Param("subject") Byte subject,
            @Param("model") String model,
            @Param("isPass") Byte isPass
    );

    /**
     * 分页查询考试记录（获取分页数据）
     */
    List<ExamRecord> findByPage(
            @Param("userId") Long userId,
            @Param("subject") Byte subject,
            @Param("model") String model,
            @Param("isPass") Byte isPass,
            @Param("startIndex") Integer startIndex,
            @Param("pageSize") Integer pageSize
    );

    /**
     * 分页查询考试记录（获取总条数）
     */
    Integer countTotalByCondition(
            @Param("userId") Long userId,
            @Param("subject") Byte subject,
            @Param("model") String model,
            @Param("isPass") Byte isPass
    );
}