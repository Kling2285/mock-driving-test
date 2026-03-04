package com.example.controller;

import com.example.common.Result;
import com.example.entity.ErrorQuestion;
import com.example.service.ErrorQuestionService;
import com.example.vo.ErrorQuestionWithQuestionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/errorQuestion")
public class ErrorQuestionController {

    @Autowired
    private ErrorQuestionService errorQuestionService;

    /**
     * 新增错题记录
     */
    @PostMapping
    public Result insert(@RequestBody ErrorQuestion errorQuestion) {
        try {
            errorQuestionService.insert(errorQuestion);
            return Result.success(errorQuestion);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("新增错题记录失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询单条错题记录
     * 关键修改：添加 /detail 固定前缀，避免与其他精确路径接口冲突
     */
    @GetMapping("/detail/{id}")
    public Result findById(@PathVariable("id") Long id) {
        try {
            ErrorQuestion errorQuestion = errorQuestionService.findById(id);
            if (errorQuestion == null) {
                return Result.error("未找到ID为" + id + "的错题记录");
            }
            return Result.success(errorQuestion);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询错题记录失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID更新错题记录
     */
    @PutMapping
    public Result updateById(@RequestBody ErrorQuestion errorQuestion) {
        try {
            errorQuestionService.updateById(errorQuestion);
            return Result.success(errorQuestion);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新错题记录失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID删除单条错题记录
     * 关键修改：添加 /detail 固定前缀，与查询单条错题路径保持一致，避免冲突
     */
    @DeleteMapping("/detail/{id}")
    public Result deleteById(@PathVariable("id") Long id) {
        try {
            errorQuestionService.deleteById(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除错题记录失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID查询该用户所有错题记录
     */
    @GetMapping("/listByUserId")
    public Result findByUserId(@RequestParam("userId") Long userId) {
        try {
            List<ErrorQuestion> list = errorQuestionService.findByUserId(userId);
            return Result.success(list);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询用户错题记录失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID+科目+驾照类型查询错题记录
     */
    @GetMapping("/listByUserIdSubjectAndModel")
    public Result findByUserIdSubjectAndModel(
            @RequestParam("userId") Long userId,
            @RequestParam("subject") Byte subject,
            @RequestParam("model") String model
    ) {
        try {
            List<ErrorQuestion> list = errorQuestionService.findByUserIdSubjectAndModel(userId, subject, model);
            return Result.success(list);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询错题记录失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID+科目统计错题总数
     */
    @GetMapping("/countByUserIdAndSubject")
    public Result countByUserIdAndSubject(
            @RequestParam("userId") Long userId,
            @RequestParam("subject") Byte subject
    ) {
        try {
            Integer count = errorQuestionService.countByUserIdAndSubject(userId, subject);
            return Result.success(count);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("统计错题总数失败：" + e.getMessage());
        }
    }

    /**
     * 联表查询：根据用户ID获取「错题+题目详情」列表
     */
    @GetMapping("/listWithQuestionByUserId")
    public Result findErrorQuestionWithQuestionByUserId(@RequestParam("userId") Long userId) {
        try {
            List<ErrorQuestionWithQuestionVO> list = errorQuestionService.getErrorQuestionWithQuestionByUserId(userId);
            return Result.success(list);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("联表查询错题+题目详情失败：" + e.getMessage());
        }
    }

    /**
     * 可选：联表查询：根据用户ID+科目+驾照类型获取「错题+题目详情」列表
     */
    @GetMapping("/listWithQuestionByUserIdSubjectAndModel")
    public Result findErrorQuestionWithQuestionByUserIdSubjectAndModel(
            @RequestParam("userId") Long userId,
            @RequestParam("subject") Byte subject,
            @RequestParam("model") String model
    ) {
        try {
            List<ErrorQuestionWithQuestionVO> list = errorQuestionService.getErrorQuestionWithQuestionByUserIdSubjectAndModel(userId, subject, model);
            return Result.success(list);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("联表查询错题+题目详情失败：" + e.getMessage());
        }
    }

    @GetMapping("/listWithQuestionByExamRecord")
    public Result findErrorQuestionWithQuestionByExamRecordId(
            @RequestParam("userId") Long userId,
            @RequestParam("subject") Byte subject,
            @RequestParam("model") String model,
            @RequestParam("examRecordId") Long examRecordId
    ) {
        try {
            List<ErrorQuestionWithQuestionVO> list = errorQuestionService.getErrorQuestionWithQuestionByExamRecordId(
                    userId, subject, model, examRecordId
            );
            return Result.success(list);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询该次考试错题失败：" + e.getMessage());
        }
    }
}