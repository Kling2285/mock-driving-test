package com.example.service.impl;

import com.example.common.PageResult;
import com.example.entity.Question;
import com.example.mapper.QuestionMapper;
import com.example.service.QuestionService;
import com.example.utils.JuheDrivingApiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 驾照题目服务实现类
 */
@Service
@Transactional
public class QuestionServiceImpl implements QuestionService {

    // 注入Mapper和API工具类
    @Autowired
    private QuestionMapper questionMapper;

    @Autowired
    private JuheDrivingApiUtil juheDrivingApiUtil;

    /**
     * 查询所有题目
     */
    @Override
    public List<Question> findAll() {
        // 直接调用Mapper查询，无复杂业务逻辑
        return questionMapper.findAll();
    }

    /**
     * 按科目+驾照类型+题型查询题目
     */
    @Override
    public List<Question> findListByCondition(Integer subject, String model, Integer questionType) {
        // 适配Mapper查询逻辑，返回符合条件的列表
        return questionMapper.selectBySubjectModelAndType(subject, model, questionType, Integer.MAX_VALUE);
    }

    /**
     * 根据题目ID查询单个题目（ID为数据库自增主键）
     */
    @Override
    public Question findById(Long id) {
        // 业务校验：ID不能为空且大于0（双重保障，避免无效查询）
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("题目ID必须大于0");
        }
        return questionMapper.findById(id);
    }

    /**
     * 新增单个题目（含业务校验）
     */
    @Override
    public void insert(Question question) {
        // 1. 业务参数校验，避免无效数据入库
        if (question == null) {
            throw new IllegalArgumentException("题目信息不能为空");
        }
        if (!StringUtils.hasText(question.getQuestion())) {
            throw new IllegalArgumentException("题目内容不能为空");
        }
        if (!StringUtils.hasText(question.getAnswer())) {
            throw new IllegalArgumentException("题目答案编号不能为空");
        }
        if (question.getSubject() == null || (question.getSubject() != 1 && question.getSubject() != 4)) {
            throw new IllegalArgumentException("科目类型只能是1（科目一）或4（科目四）");
        }
        if (!StringUtils.hasText(question.getModel())) {
            throw new IllegalArgumentException("驾照类型不能为空");
        }
        if (question.getQuestionType() == null || (question.getQuestionType() < 1 || question.getQuestionType() > 3)) {
            throw new IllegalArgumentException("题型只能是1（判断题）、2（单选题）、3（多选题）");
        }

        // 【删除：自增主键无需校验ID唯一性，API原始ID存储在apiId字段】
        // Question existQuestion = questionMapper.findById(question.getId());
        // if (existQuestion != null) {
        //     throw new RuntimeException("题目ID" + question.getId() + "已存在，无法新增");
        // }

        // 3. 调用Mapper新增
        questionMapper.batchInsert(new ArrayList<Question>(){{add(question);}});
    }

    /**
     * 更新题目（含业务校验）
     */
    @Override
    public void updateById(Question question) {
        // 1. 基础校验
        if (question == null || question.getId() == null || question.getId() <= 0) {
            throw new IllegalArgumentException("题目ID必须大于0");
        }

        // 2. 校验题目是否存在
        Question existQuestion = questionMapper.findById(question.getId());
        if (existQuestion == null) {
            throw new RuntimeException("要更新的题目不存在（ID：" + question.getId() + "）");
        }

        // 3. 核心业务校验（关键字段非空校验）
        if (!StringUtils.hasText(question.getQuestion())) {
            throw new IllegalArgumentException("题目内容不能为空");
        }
        if (!StringUtils.hasText(question.getAnswer())) {
            throw new IllegalArgumentException("题目答案编号不能为空");
        }

        // 4. 调用Mapper更新
        questionMapper.updateById(question);
    }

    /**
     * 根据题目ID删除单个题目
     */
    @Override
    public void deleteById(Long id) {
        // 1. 基础校验
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("题目ID必须大于0");
        }

        // 2. 校验题目是否存在
        Question existQuestion = questionMapper.findById(id);
        if (existQuestion == null) {
            throw new RuntimeException("要删除的题目不存在（ID：" + id + "）");
        }

        // 3. 调用Mapper删除
        questionMapper.deleteById(id);
    }

    /**
     * 题目分页查询（模仿AdminServiceImpl分页逻辑）
     */
    @Override
    public PageResult<Question> findQuestionByPage(Integer subject, String model, Integer questionType, Integer pageNum, Integer pageSize) {
        // 1. 分页参数校验 & 默认值设置，防止空值/非法值报错
        pageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        pageSize = (pageSize == null || pageSize < 1 || pageSize > 100) ? 10 : pageSize;

        // 2. 复用条件查询逻辑，获取所有符合条件的数据
        List<Question> allList = this.findListByCondition(subject, model, questionType);
        if (allList == null) {
            allList = new ArrayList<>(); // 避免空指针异常
        }

        // 3. 纯Java分页核心逻辑（与AdminServiceImpl完全一致）
        int totalCount = allList.size();
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalCount);
        List<Question> pageList = startIndex >= totalCount ? new ArrayList<>() : allList.subList(startIndex, endIndex);

        // 4. 封装分页结果返回
        return new PageResult<>(pageNum, pageSize, (long) totalCount, pageList);
    }

    /**
     * 按科目+驾照类型查询题目
     */
    @Override
    public List<Question> findBySubjectAndModel(Integer subject, String model) {
        // 业务校验
        if (subject == null || (subject != 1 && subject != 4)) {
            throw new IllegalArgumentException("科目类型只能是1（科目一）或4（科目四）");
        }
        if (!StringUtils.hasText(model)) {
            throw new IllegalArgumentException("驾照类型不能为空");
        }
        return questionMapper.findBySubjectAndModel(subject, model);
    }

    /**
     * 题库同步核心方法：将API数据同步到本地数据库（事务保障）
     */
    @Override
    public void syncQuestionBankFromApi(Integer subject, String model) {
        // 1. 同步参数校验
        if (subject == null || (subject != 1 && subject != 4)) {
            throw new IllegalArgumentException("科目类型只能是1（科目一）或4（科目四）");
        }
        if (!StringUtils.hasText(model)) {
            throw new IllegalArgumentException("驾照类型不能为空");
        }

        try {
            // 2. 调用API工具类获取该科目+驾照类型的全部题目
            System.out.println("开始从聚合数据API获取【科目" + subject + "-" + model + "】题库数据...");
            List<Question> apiQuestionList = juheDrivingApiUtil.getQuestionList(subject, model, "order");

            // 3. 校验API返回数据是否为空
            if (CollectionUtils.isEmpty(apiQuestionList)) {
                throw new RuntimeException("聚合数据API未返回【科目" + subject + "-" + model + "】有效题目数据，同步终止");
            }
            System.out.println("API获取题目成功，共获取 " + apiQuestionList.size() + " 道题目");

            // 4. 删除本地旧题库数据，避免重复
            System.out.println("开始删除本地数据库【科目" + subject + "-" + model + "】旧题库数据...");
            questionMapper.deleteBySubjectAndModel(subject, model);
            System.out.println("本地旧题库数据删除成功");

            // 5. 批量插入新题库数据
            System.out.println("开始批量插入【科目" + subject + "-" + model + "】新题库数据...");
            questionMapper.batchInsert(apiQuestionList);
            System.out.println("题库同步成功！共插入 " + apiQuestionList.size() + " 道题目到本地数据库");

        } catch (Exception e) {
            // 异常捕获，打印日志并抛出运行时异常（触发事务回滚）
            System.err.println("【科目" + subject + "-" + model + "】题库同步失败：" + e.getMessage());
            throw new RuntimeException("题库同步失败：" + e.getMessage(), e);
        }
    }

    /**
     * 按考试规则生成模拟试卷
     */
    @Override
    public List<Question> generateExamPaper(Integer subject, String model) {
        // 1. 组卷参数校验
        if (subject == null || (subject != 1 && subject != 4)) {
            throw new IllegalArgumentException("科目类型只能是1（科目一）或4（科目四）");
        }
        if (!StringUtils.hasText(model)) {
            throw new IllegalArgumentException("驾照类型不能为空");
        }

        // 从本地数据库查询题目组卷（规避API调用限制，与Admin业务逻辑对齐）
        List<Question> allQuestionList = this.findBySubjectAndModel(subject, model);
        if (CollectionUtils.isEmpty(allQuestionList)) {
            throw new RuntimeException("本地数据库中【科目" + subject + "-" + model + "】暂无题目数据，无法生成试卷");
        }

        // 2. 按考试规则筛选题目
        List<Question> examPaperList = new ArrayList<>();
        if (subject == 1) {
            // 科目一：40道判断题 + 60道单选题
            List<Question> judgeQuestions = allQuestionList.stream()
                    .filter(q -> q.getQuestionType() == 1)
                    .limit(40)
                    .toList();
            List<Question> singleChoiceQuestions = allQuestionList.stream()
                    .filter(q -> q.getQuestionType() == 2)
                    .limit(60)
                    .toList();

            examPaperList.addAll(judgeQuestions);
            examPaperList.addAll(singleChoiceQuestions);
        } else if (subject == 4) {
            // 科目四：20道判断题 + 20道单选题 + 10道多选题
            List<Question> judgeQuestions = allQuestionList.stream()
                    .filter(q -> q.getQuestionType() == 1)
                    .limit(20)
                    .toList();
            List<Question> singleChoiceQuestions = allQuestionList.stream()
                    .filter(q -> q.getQuestionType() == 2)
                    .limit(20)
                    .toList();
            List<Question> multiChoiceQuestions = allQuestionList.stream()
                    .filter(q -> q.getQuestionType() == 3)
                    .limit(10)
                    .toList();

            examPaperList.addAll(judgeQuestions);
            examPaperList.addAll(singleChoiceQuestions);
            examPaperList.addAll(multiChoiceQuestions);
        }

        return examPaperList;
    }


    /**
     * 按指定题型生成模拟试卷（100道题，仅包含单一指定题型，随机选取）
     * @param subject 科目类型（1=科目一，4=科目四）
     * @param model 驾照类型（c1/c2/a1/a2）
     * @param questionType 题型（1=判断题，2=单选题，3=多选题）
     * @return 100道对应题型的随机题目列表
     */
    @Override
    public List<Question> generateSpecialTypeExamPaper(Integer subject, String model, Integer questionType) {

        if (subject == null || (subject != 1 && subject != 4)) {
            throw new IllegalArgumentException("科目类型只能是1（科目一）或4（科目四）");
        }
        // 驾照类型校验
        if (!StringUtils.hasText(model)) {
            throw new IllegalArgumentException("驾照类型不能为空");
        }
        // 题型校验
        if (questionType == null || (questionType < 1 || questionType > 3)) {
            throw new IllegalArgumentException("题型只能是1（判断题）、2（单选题）、3（多选题）");
        }

        // 2. 查询对应科目+驾照类型+题型的所有题目
        List<Question> targetTypeQuestionList = this.findListByCondition(subject, model, questionType);
        // 校验查询结果是否为空
        if (CollectionUtils.isEmpty(targetTypeQuestionList)) {
            throw new RuntimeException("本地数据库中【科目" + subject + "-" + model + "-题型" + questionType + "】暂无题目数据，无法生成试卷");
        }

        // 3. 生成100道题：随机选取
        List<Question> specialExamPaperList = new ArrayList<>();
        int totalNeed = 100; // 需求100道题
        int currentCount = targetTypeQuestionList.size();

        // 第一步：先将目标题型题目列表复制并打乱
        List<Question> shuffledQuestionList = new ArrayList<>(targetTypeQuestionList);
        Collections.shuffle(shuffledQuestionList);

        if (currentCount >= totalNeed) {
            // 题目充足：从打乱后的列表中取前100道
            specialExamPaperList = shuffledQuestionList.stream()
                    .limit(totalNeed)
                    .toList();
        } else {
            // 题目不足100道：基于打乱后的列表循环填充，保证随机性
            for (int i = 0; i < totalNeed; i++) {
                int index = i % currentCount;
                specialExamPaperList.add(shuffledQuestionList.get(index));
            }
        }

        return specialExamPaperList;
    }

    @Override
    public Question findBySubjectModelAndApiId(Integer subject, String model, String apiId) {
        // 1. 业务参数校验（与现有Service校验风格一致，避免无效查询）
        if (subject == null || (subject != 1 && subject != 4)) {
            throw new IllegalArgumentException("科目类型只能是1（科目一）或4（科目四）");
        }
        if (!StringUtils.hasText(model)) {
            throw new IllegalArgumentException("驾照类型不能为空");
        }
        if (!StringUtils.hasText(apiId)) {
            throw new IllegalArgumentException("题目API编号不能为空");
        }

        // 2. 调用Mapper层方法完成查询
        return questionMapper.findBySubjectModelAndApiId(subject, model, apiId);
    }



}