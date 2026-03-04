package com.example.service.impl;

import com.example.common.PageResult;
import com.example.entity.ExamRecord;
import com.example.mapper.ExamRecordMapper;
import com.example.service.ExamRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ExamRecordServiceImpl implements ExamRecordService {

    @Autowired
    private ExamRecordMapper examRecordMapper;

    @Override
    public void insert(ExamRecord examRecord) {
        if (examRecord == null) {
            throw new IllegalArgumentException("考试记录信息不能为空");
        }
        if (examRecord.getUserId() == null || examRecord.getUserId() <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
        examRecordMapper.insert(examRecord);
    }

    @Override
    public ExamRecord findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("考试记录ID必须大于0");
        }
        return examRecordMapper.findById(id);
    }

    @Override
    public void updateById(ExamRecord examRecord) {
        if (examRecord == null || examRecord.getId() == null || examRecord.getId() <= 0) {
            throw new IllegalArgumentException("考试记录ID必须大于0");
        }
        ExamRecord existRecord = examRecordMapper.findById(examRecord.getId());
        if (existRecord == null) {
            throw new RuntimeException("要更新的考试记录不存在（ID：" + examRecord.getId() + "）");
        }
        examRecordMapper.updateById(examRecord);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("考试记录ID必须大于0");
        }
        ExamRecord existRecord = examRecordMapper.findById(id);
        if (existRecord == null) {
            throw new RuntimeException("要删除的考试记录不存在（ID：" + id + "）");
        }
        examRecordMapper.deleteById(id);
    }

    @Override
    public List<ExamRecord> findByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
        return examRecordMapper.findByUserId(userId);
    }

    @Override
    public List<ExamRecord> findByUserIdSubjectAndModel(Long userId, Byte subject, String model) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
        if (subject == null || (subject != 1 && subject != 4)) {
            throw new IllegalArgumentException("科目类型只能是1（科目一）或4（科目四）");
        }
        if (!StringUtils.hasText(model)) {
            throw new IllegalArgumentException("驾照类型不能为空");
        }
        return examRecordMapper.findByUserIdSubjectAndModel(userId, subject, model);
    }

    @Override
    public Integer countTotalByUserIdAndSubject(Long userId, Byte subject) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
        if (subject == null || (subject != 1 && subject != 4)) {
            throw new IllegalArgumentException("科目类型只能是1（科目一）或4（科目四）");
        }
        Integer total = examRecordMapper.countTotalByUserIdAndSubject(userId, subject);
        return total == null ? 0 : total;
    }

    @Override
    public Integer countMaxScoreByUserIdAndSubject(Long userId, Byte subject) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
        if (subject == null || (subject != 1 && subject != 4)) {
            throw new IllegalArgumentException("科目类型只能是1（科目一）或4（科目四）");
        }
        return examRecordMapper.countMaxScoreByUserIdAndSubject(userId, subject);
    }

    @Override
    public Integer countPassedByUserIdAndSubject(Long userId, Byte subject) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
        if (subject == null || (subject != 1 && subject != 4)) {
            throw new IllegalArgumentException("科目类型只能是1（科目一）或4（科目四）");
        }
        Integer passed = examRecordMapper.countPassedByUserIdAndSubject(userId, subject);
        return passed == null ? 0 : passed;
    }

    @Override
    public Double calculatePassRateByUserIdAndSubject(Long userId, Byte subject) {
        Integer total = this.countTotalByUserIdAndSubject(userId, subject);
        if (total == 0) {
            return 0.0;
        }
        Integer passed = this.countPassedByUserIdAndSubject(userId, subject);
        return (double) passed / total * 100;
    }

    @Override
    public ExamRecord findLatestByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
        return examRecordMapper.findLatestByUserId(userId);
    }

    @Override
    public List<ExamRecord> findListByCondition(Long userId, Byte subject, String model, Byte isPass) {
        return examRecordMapper.findListByCondition(userId, subject, model, isPass);
    }

    @Override
    public PageResult<ExamRecord> findByPage(Long userId, Byte subject, String model, Byte isPass, Integer pageNum, Integer pageSize) {
        // 分页参数校验与默认值设置
        pageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        pageSize = (pageSize == null || pageSize < 1 || pageSize > 100) ? 10 : pageSize;
        int startIndex = (pageNum - 1) * pageSize;

        // 查询分页数据与总条数
        List<ExamRecord> pageList = examRecordMapper.findByPage(userId, subject, model, isPass, startIndex, pageSize);
        Integer totalCount = examRecordMapper.countTotalByCondition(userId, subject, model, isPass);
        totalCount = totalCount == null ? 0 : totalCount;

        // 封装分页结果
        return new PageResult<>(pageNum, pageSize, (long) totalCount, pageList == null ? new ArrayList<>() : pageList);
    }
}
