package com.example.entity;

import lombok.Data;

/**
 * 驾照题目实体类（科目一/科目四通用）
 */
@Data
public class Question {
    private Long id; // 题目ID（API返回）
    private Long apiId;
    private String question; // 题目内容
    private String answer; // 答案编号（1=A/正确，2=B/错误，3=C，4=D等，对应答案映射接口）
    private String item1; // 选项1（判断题时：item1=正确，item2=错误）
    private String item2; // 选项2
    private String item3; // 选项3（单选题/多选题才有，判断题为空）
    private String item4; // 选项4（单选题/多选题才有，判断题为空）
    private String explains; // 答案解析（错题展示时使用）
    private String url; // 题目图片URL（可选，部分题目有配图）
    private Integer subject; // 本地补充：科目类型（1=科目一，4=科目四）
    private String model; // 本地补充：驾照类型（c1/c2/a1等）
    private Integer questionType; // 本地补充：题型（1=判断题，2=单选题，3=多选题）- 可通过item3/item4是否为空判断
}