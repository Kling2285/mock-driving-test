package com.example.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 错题+题目详情
 */
@Data
public class ErrorQuestionWithQuestionVO {
    // ========== 错题表（error_question）字段 ==========
    private Long id; // 错题主键ID
    private Long userId; // 用户ID
    private String apiId; // 题目API编号
    private Byte subject; // 科目
    private String model; // 驾照类型
    private String userAnswer; // 用户错误答案
    private String correctAnswer; // 正确答案（错题表冗余字段）
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime answerTime; // 答题时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createTime; // 错题记录创建时间
    private Long examRecordId;

    // ========== 题目表（question）字段 ==========
    private String questionContent; // 题目内容（对应q.question）
    private String optionA; // 选项1（对应q.item1）
    private String optionB; // 选项2（对应q.item2）
    private String optionC; // 选项3（对应q.item3）
    private String optionD; // 选项4（对应q.item4）
    private String analysis; // 题目解析（对应q.explains）
    private String questionUrl; // 题目图片URL（新增，对应q.url，可选）
    private Integer questionType; // 题型（新增，对应q.question_type，可选）
}