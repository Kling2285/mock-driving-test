package com.example.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.example.common.PageResult;
import com.example.common.Result;
import com.example.entity.Question;
import com.example.service.QuestionService;
import com.example.utils.ExcelUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 驾照题目控制器（模仿AdminController风格，规范接口定义）
 */
@Controller
@RequestMapping("/question")
@ResponseBody
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    // 1. 查询所有题目（无分页）
    @GetMapping("/list")
    public Result list() {
        try {
            List<Question> list = questionService.findAll();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("查询所有题目失败：" + e.getMessage());
        }
    }

    // 2. 按科目+驾照类型+题型条件查询题目
    @GetMapping("/listByCondition")
    public Result listByCondition(
            @RequestParam(value = "subject", required = false) Integer subject,
            @RequestParam(value = "model", required = false) String model,
            @RequestParam(value = "questionType", required = false) Integer questionType
    ) {
        try {
            List<Question> list = questionService.findListByCondition(subject, model, questionType);
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("条件查询题目失败：" + e.getMessage());
        }
    }

    // 3. 新增单个题目
    @PostMapping
    public Result add(@RequestBody Question question) {
        try {
            // 前置参数简易校验（与Service层双重校验）
            if (!StringUtils.hasText(question.getQuestion())) {
                return Result.error("题目内容不能为空");
            }
            if (!StringUtils.hasText(question.getAnswer())) {
                return Result.error("题目答案编号不能为空");
            }
            if (question.getSubject() == null || (question.getSubject() != 1 && question.getSubject() != 4)) {
                return Result.error("科目类型只能是1（科目一）或4（科目四）");
            }
            if (!StringUtils.hasText(question.getModel())) {
                return Result.error("驾照类型不能为空");
            }

            // 关键：新增时清空前端可能传入的id（强制由数据库自增生成）
            question.setId(null);

            questionService.insert(question);
            return Result.success(question);
        } catch (Exception e) {
            return Result.error("新增题目失败：" + e.getMessage());
        }
    }

    // 4. 根据题目ID查询单个题目（路径传参）
    @GetMapping("/{id}")
    public Result findOne(@PathVariable("id") Long id) {
        try {
            if (id == null || id <= 0) {
                return Result.error("题目ID必须大于0");
            }

            Question question = questionService.findById(id);
            if (question == null) {
                return Result.error("未找到ID为" + id + "的题目");
            }
            return Result.success(question);
        } catch (Exception e) {
            return Result.error("查询题目失败：" + e.getMessage());
        }
    }

    // 5. 更新题目
    @PutMapping
    public Result update(@RequestBody Question question) {
        try {
            // 参数校验
            if (question.getId() == null || question.getId() <= 0) {
                return Result.error("题目ID必须大于0");
            }
            // 先校验题目是否存在
            Question existQuestion = questionService.findById(question.getId());
            if (existQuestion == null) {
                return Result.error("要更新的题目不存在");
            }

            questionService.updateById(question);
            return Result.success(question);
        } catch (Exception e) {
            return Result.error("更新题目失败：" + e.getMessage());
        }
    }

    // 6. 根据题目ID删除单个题目
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable("id") Long id) {
        try {
            if (id == null || id <= 0) {
                return Result.error("题目ID必须大于0");
            }
            Question existQuestion = questionService.findById(id);
            if (existQuestion == null) {
                return Result.error("要删除的题目不存在");
            }

            questionService.deleteById(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除题目失败：" + e.getMessage());
        }
    }

    // 7. 题目分页查询
    @GetMapping("/page")
    public Result findQuestionByPage(
            @RequestParam(value = "subject", required = false) Integer subject,
            @RequestParam(value = "model", required = false) String model,
            @RequestParam(value = "questionType", required = false) Integer questionType,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize
    ) {
        try {
            PageResult<Question> pageResult = questionService.findQuestionByPage(subject, model, questionType, pageNum, pageSize);
            return Result.success(pageResult);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("分页查询题目失败：" + e.getMessage());
        }
    }

    // 8. 按科目+驾照类型查询题目
    @GetMapping("/listBySubjectAndModel")
    public Result listBySubjectAndModel(
            @RequestParam(value = "subject") Integer subject,
            @RequestParam(value = "model") String model
    ) {
        try {
            List<Question> list = questionService.findBySubjectAndModel(subject, model);
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("按科目+驾照类型查询题目失败：" + e.getMessage());
        }
    }

    // 9. 题库同步接口（核心功能：从API同步到本地数据库）
    @PostMapping("/sync")
    public Result syncQuestionBankFromApi(
            @RequestParam(value = "subject") Integer subject,
            @RequestParam(value = "model") String model
    ) {
        try {
            questionService.syncQuestionBankFromApi(subject, model);
            return Result.success("题库同步成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("题库同步失败：" + e.getMessage());
        }
    }

    // 10. 生成模拟试卷接口
    @GetMapping("/generateExamPaper")
    public Result generateExamPaper(
            @RequestParam(value = "subject") Integer subject,
            @RequestParam(value = "model") String model
    ) {
        try {
            List<Question> examPaperList = questionService.generateExamPaper(subject, model);
            return Result.success(examPaperList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("生成模拟试卷失败：" + e.getMessage());
        }
    }

    //生成题型的试卷
    @GetMapping("/generateSpecialTypeExamPaper")
    public Result generateSpecialTypeExamPaper(
            @RequestParam(value = "subject") Integer subject,
            @RequestParam(value = "model") String model,
            @RequestParam(value = "questionType") Integer questionType
    ) {
        try {
            // 调用 Service 层方法，生成指定题型的100道题试卷
            List<Question> specialTypeExamPaperList = questionService.generateSpecialTypeExamPaper(subject, model, questionType);
            // 返回成功结果，携带题目列表
            return Result.success(specialTypeExamPaperList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("生成模拟试卷失败：" + e.getMessage());
        }
    }

    // 11. 导出题目Excel（带条件筛选）
    @GetMapping("/export")
    public void export(HttpServletResponse response,
                       @RequestParam(value = "subject", required = false) Integer subject,
                       @RequestParam(value = "model", required = false) String model,
                       @RequestParam(value = "questionType", required = false) Integer questionType) throws IOException {
        try {
            // 条件查询要导出的数据
            List<Question> list = questionService.findListByCondition(subject, model, questionType);
            ExcelUtils.export(response, list, "驾照题目列表");
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(Result.error("导出Excel失败：" + e.getMessage()).toString());
        }
    }

    // 12. 导入题目Excel（POST + 文件上传）
    @PostMapping("/import")
    public Result importExcel(@RequestParam("file") MultipartFile file) {
        try {
            // 参数校验：文件不能为空
            if (file.isEmpty()) {
                return Result.error("上传的Excel文件不能为空");
            }
            // 校验文件类型（可选）
            String originalFilename = file.getOriginalFilename();
            if (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls")) {
                return Result.error("仅支持.xlsx/.xls格式的Excel文件");
            }

            // 读取Excel文件
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
            List<Question> list = reader.readAll(Question.class);

            // 批量导入
            Integer count = 0;
            for (Question item : list) {
                // 关键：导入时清空id（强制由数据库自增生成），避免Excel中旧ID导致冲突
                item.setId(null);
                // 简单校验必填字段
                if (StringUtils.hasText(item.getQuestion()) && StringUtils.hasText(item.getAnswer())
                        && item.getSubject() != null && StringUtils.hasText(item.getModel())) {
                    questionService.insert(item);
                    count++;
                }
            }

            // 返回导入成功的条数
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("count", count);
            resultMap.put("total", list.size()); // 总读取条数
            return Result.success(resultMap);
        } catch (Exception e) {
            return Result.error("导入Excel失败：" + e.getMessage());
        }
    }

    @GetMapping("/byApiId")
    public Result findBySubjectModelAndApiId(
            @RequestParam("subject") Integer subject,
            @RequestParam("model") String model,
            @RequestParam("apiId") String apiId
    ) {
        try {
            // 前端参数校验（与Service层双重校验，快速返回错误信息）
            if (subject == null || (subject != 1 && subject != 4)) {
                return Result.error("科目类型只能是1（科目一）或4（科目四）");
            }
            if (!StringUtils.hasText(model)) {
                return Result.error("驾照类型不能为空");
            }
            if (!StringUtils.hasText(apiId)) {
                return Result.error("题目API编号不能为空");
            }

            // 调用Service层方法查询
            Question question = questionService.findBySubjectModelAndApiId(subject, model, apiId);
            if (question == null) {
                return Result.error("未找到该科目+驾照类型+API编号对应的题目");
            }
            return Result.success(question);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("查询题目失败：" + e.getMessage());
        }
    }

}