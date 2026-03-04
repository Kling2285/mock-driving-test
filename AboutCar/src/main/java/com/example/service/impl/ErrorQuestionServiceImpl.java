package com.example.service.impl;

import com.example.entity.ErrorQuestion;
import com.example.mapper.ErrorQuestionMapper;
import com.example.service.ErrorQuestionService;
import com.example.vo.ErrorQuestionWithQuestionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
public class ErrorQuestionServiceImpl implements ErrorQuestionService {

    @Autowired
    private ErrorQuestionMapper errorQuestionMapper;

    @Override
    public void insert(ErrorQuestion errorQuestion) {
        // 参数合法性校验
        if (errorQuestion == null) {
            throw new IllegalArgumentException("错题记录信息不能为空");
        }
        if (errorQuestion.getUserId() == null || errorQuestion.getUserId() <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
        if (!StringUtils.hasText(errorQuestion.getApiId())) {
            throw new IllegalArgumentException("题目API编号不能为空");
        }
        if (errorQuestion.getSubject() == null || (errorQuestion.getSubject() != 1 && errorQuestion.getSubject() != 4)) {
            throw new IllegalArgumentException("科目类型只能是1（科目一）或4（科目四）");
        }
        if (!StringUtils.hasText(errorQuestion.getModel())) {
            throw new IllegalArgumentException("驾照类型不能为空");
        }
        errorQuestionMapper.insert(errorQuestion);
    }

    @Override
    public ErrorQuestion findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("错题记录ID必须大于0");
        }
        return errorQuestionMapper.findById(id);
    }

    @Override
    public void updateById(ErrorQuestion errorQuestion) {
        if (errorQuestion == null || errorQuestion.getId() == null || errorQuestion.getId() <= 0) {
            throw new IllegalArgumentException("错题记录ID必须大于0");
        }
        // 校验错题记录是否存在
        ErrorQuestion existError = errorQuestionMapper.findById(errorQuestion.getId());
        if (existError == null) {
            throw new RuntimeException("要更新的错题记录不存在（ID：" + errorQuestion.getId() + "）");
        }
        errorQuestionMapper.updateById(errorQuestion);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("错题记录ID必须大于0");
        }
        // 校验错题记录是否存在
        ErrorQuestion existError = errorQuestionMapper.findById(id);
        if (existError == null) {
            throw new RuntimeException("要删除的错题记录不存在（ID：" + id + "）");
        }
        errorQuestionMapper.deleteById(id);
    }

    @Override
    public List<ErrorQuestion> findByUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
        return errorQuestionMapper.findByUserId(userId);
    }

    @Override
    public List<ErrorQuestion> findByUserIdSubjectAndModel(Long userId, Byte subject, String model) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
        if (subject == null || (subject != 1 && subject != 4)) {
            throw new IllegalArgumentException("科目类型只能是1（科目一）或4（科目四）");
        }
        if (!StringUtils.hasText(model)) {
            throw new IllegalArgumentException("驾照类型不能为空");
        }
        return errorQuestionMapper.findByUserIdSubjectAndModel(userId, subject, model);
    }

    @Override
    public Integer countByUserIdAndSubject(Long userId, Byte subject) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
        if (subject == null || (subject != 1 && subject != 4)) {
            throw new IllegalArgumentException("科目类型只能是1（科目一）或4（科目四）");
        }
        Integer count = errorQuestionMapper.countByUserIdAndSubject(userId, subject);
        return count == null ? 0 : count;
    }

    @Override
    public List<ErrorQuestionWithQuestionVO> getErrorQuestionWithQuestionByUserId(Long userId) {
        // 非空校验
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空且必须为正数");
        }
        return errorQuestionMapper.findErrorQuestionWithQuestionByUserId(userId);
    }

    @Override
    public List<ErrorQuestionWithQuestionVO> getErrorQuestionWithQuestionByUserIdSubjectAndModel(Long userId, Byte subject, String model) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空且必须为正数");
        }
        if (subject == null || (subject != 1 && subject != 4)) {
            throw new IllegalArgumentException("科目只能是1（科目一）或4（科目四）");
        }
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("驾照类型不能为空");
        }
        return errorQuestionMapper.findErrorQuestionWithQuestionByUserIdSubjectAndModel(userId, subject, model);
    }

    @Override
    public List<ErrorQuestionWithQuestionVO> getErrorQuestionWithQuestionByExamRecordId(
            Long userId,
            Byte subject,
            String model,
            Long examRecordId
    ) {
        // 参数合法性校验（确保查询条件有效）
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID不能为空且必须为正数");
        }
        if (subject == null || (subject != 1 && subject != 4)) {
            throw new IllegalArgumentException("科目只能是1（科目一）或4（科目四）");
        }
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("驾照类型不能为空");
        }
        if (examRecordId == null || examRecordId <= 0) {
            throw new IllegalArgumentException("考试记录ID不能为空且必须为正数");
        }
        // 调用Mapper方法查询该次考试的错题
        return errorQuestionMapper.findErrorQuestionWithQuestionByExamRecordId(userId, subject, model, examRecordId);
    }


}
