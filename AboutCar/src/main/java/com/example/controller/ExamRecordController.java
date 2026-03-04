package com.example.controller;

import com.example.common.PageResult;
import com.example.common.Result;
import com.example.entity.ExamRecord;
import com.example.service.ExamRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/examRecord")
public class ExamRecordController {

    @Autowired
    private ExamRecordService examRecordService;

    /**
     * 新增考试记录
     */
    @PostMapping
    public Result insert(@RequestBody ExamRecord examRecord) {
        try {
            examRecordService.insert(examRecord);
            Long newExamRecordId = examRecord.getId();
            return Result.success(newExamRecordId);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("新增考试记录失败：" + e.getMessage());
        }
    }
    /**
     * 根据ID查询单条考试记录
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable("id") Long id) {
        try {
            ExamRecord examRecord = examRecordService.findById(id);
            if (examRecord == null) {
                return Result.error("未找到ID为" + id + "的考试记录");
            }
            return Result.success(examRecord);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询考试记录失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID更新考试记录
     */
    @PutMapping
    public Result updateById(@RequestBody ExamRecord examRecord) {
        try {
            examRecordService.updateById(examRecord);
            return Result.success(examRecord);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新考试记录失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID删除考试记录
     */
    @DeleteMapping("/{id}")
    public Result deleteById(@PathVariable("id") Long id) {
        try {
            examRecordService.deleteById(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("删除考试记录失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID查询该用户所有考试记录
     */
    @GetMapping("/listByUserId")
    public Result findByUserId(@RequestParam("userId") Long userId) {
        try {
            List<ExamRecord> list = examRecordService.findByUserId(userId);
            return Result.success(list);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询用户考试记录失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID+科目+驾照类型查询考试记录
     */
    @GetMapping("/listByUserIdSubjectAndModel")
    public Result findByUserIdSubjectAndModel(
            @RequestParam("userId") Long userId,
            @RequestParam("subject") Byte subject,
            @RequestParam("model") String model
    ) {
        try {
            List<ExamRecord> list = examRecordService.findByUserIdSubjectAndModel(userId, subject, model);
            return Result.success(list);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询考试记录失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID+科目统计考试总次数
     */
    @GetMapping("/countTotal")
    public Result countTotalByUserIdAndSubject(
            @RequestParam("userId") Long userId,
            @RequestParam("subject") Byte subject
    ) {
        try {
            Integer total = examRecordService.countTotalByUserIdAndSubject(userId, subject);
            return Result.success(total);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("统计考试总次数失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID+科目统计最高分
     */
    @GetMapping("/countMaxScore")
    public Result countMaxScoreByUserIdAndSubject(
            @RequestParam("userId") Long userId,
            @RequestParam("subject") Byte subject
    ) {
        try {
            Integer maxScore = examRecordService.countMaxScoreByUserIdAndSubject(userId, subject);
            return Result.success(maxScore);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("统计最高分失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID+科目统计通过次数
     */
    @GetMapping("/countPassed")
    public Result countPassedByUserIdAndSubject(
            @RequestParam("userId") Long userId,
            @RequestParam("subject") Byte subject
    ) {
        try {
            Integer passed = examRecordService.countPassedByUserIdAndSubject(userId, subject);
            return Result.success(passed);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("统计通过次数失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID+科目计算通过率
     */
    @GetMapping("/calculatePassRate")
    public Result calculatePassRateByUserIdAndSubject(
            @RequestParam("userId") Long userId,
            @RequestParam("subject") Byte subject
    ) {
        try {
            Double passRate = examRecordService.calculatePassRateByUserIdAndSubject(userId, subject);
            return Result.success(passRate);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("计算通过率失败：" + e.getMessage());
        }
    }

    /**
     * 根据用户ID查询最新一条考试记录
     */
    @GetMapping("/latest")
    public Result findLatestByUserId(@RequestParam("userId") Long userId) {
        try {
            ExamRecord examRecord = examRecordService.findLatestByUserId(userId);
            return Result.success(examRecord);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询最新考试记录失败：" + e.getMessage());
        }
    }

    /**
     * 批量查询考试记录（多条件组合）
     */
    @GetMapping("/listByCondition")
    public Result findListByCondition(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "subject", required = false) Byte subject,
            @RequestParam(value = "model", required = false) String model,
            @RequestParam(value = "isPass", required = false) Byte isPass
    ) {
        try {
            List<ExamRecord> list = examRecordService.findListByCondition(userId, subject, model, isPass);
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("条件查询考试记录失败：" + e.getMessage());
        }
    }

    /**
     * 分页查询考试记录
     */
    @GetMapping("/page")
    public Result findByPage(
            @RequestParam(value = "userId", required = false) Long userId,
            @RequestParam(value = "subject", required = false) Byte subject,
            @RequestParam(value = "model", required = false) String model,
            @RequestParam(value = "isPass", required = false) Byte isPass,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize
    ) {
        try {
            PageResult<ExamRecord> pageResult = examRecordService.findByPage(userId, subject, model, isPass, pageNum, pageSize);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error("分页查询考试记录失败：" + e.getMessage());
        }
    }
}