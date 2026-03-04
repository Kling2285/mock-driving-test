/*
 Navicat Premium Dump SQL

 Source Server         : javaweb项目作业
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3308
 Source Schema         : aboutcar

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 02/01/2026 09:35:26
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `admin_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '管理员ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号（唯一）',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '邮箱（唯一）',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '手机号（唯一）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `role_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联角色表ID（控制权限）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '管理员真实姓名',
  `user_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'admin' COMMENT '用户类型：admin-管理员',
  PRIMARY KEY (`admin_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for error_question
-- ----------------------------
DROP TABLE IF EXISTS `error_question`;
CREATE TABLE `error_question`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID（关联user表的user_id主键）',
  `api_id` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '错题API编号（关联question表的api_id字段）',
  `subject` tinyint NOT NULL COMMENT '科目（1=科目一，4=科目四，与question表subject一致）',
  `model` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'c1' COMMENT '驾照类型（与question表model一致）',
  `user_answer` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户错误答案（如1/2/3/4，多选题可存\"1,3\"）',
  `correct_answer` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '正确答案（冗余存储，避免关联查询）',
  `answer_time` datetime NOT NULL COMMENT '答题时间（用户做错该题的时间）',
  `exam_record_id` bigint NOT NULL COMMENT '考试记录ID（关联 exam_record 表的主键 id，标记该错题归属的模拟考试）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_question`(`user_id` ASC, `api_id` ASC, `subject` ASC, `model` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_api_id`(`api_id` ASC) USING BTREE,
  INDEX `idx_subject`(`subject` ASC) USING BTREE,
  INDEX `idx_model`(`model` ASC) USING BTREE,
  INDEX `idx_api_subject_model`(`api_id` ASC, `subject` ASC, `model` ASC) USING BTREE,
  INDEX `idx_exam_record_id`(`exam_record_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 99 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户错题记录表（关联question.api_id）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for exam_record
-- ----------------------------
DROP TABLE IF EXISTS `exam_record`;
CREATE TABLE `exam_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID（关联user表的user_id）',
  `subject` tinyint NOT NULL COMMENT '考试科目（1=科目一，4=科目四）',
  `model` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'c1' COMMENT '驾照类型（c1/c2等）',
  `total_question` int NOT NULL COMMENT '总题数',
  `correct_count` int NOT NULL COMMENT '做对题数',
  `error_count` int NOT NULL COMMENT '做错题数',
  `score` int NULL DEFAULT NULL COMMENT '考试得分（满分100分）',
  `is_pass` tinyint NOT NULL COMMENT '是否合格（0=不合格，1=合格）',
  `exam_time` datetime NOT NULL COMMENT '考试时间（默认当前时间）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_exam_time`(`exam_time` ASC) USING BTREE,
  INDEX `idx_subject`(`subject` ASC) USING BTREE,
  CONSTRAINT `exam_record_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户模拟考试历史记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for question
-- ----------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目ID（聚合数据API返回的原始ID）',
  `api_id` bigint NULL DEFAULT NULL COMMENT '聚合数据API返回的原始题目ID',
  `question` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题目内容（最长支持1000个字符，适配长题目）',
  `answer` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '答案编号（1=A/正确，2=B/错误，3=C，4=D等，支持多选题编号如7=AB）',
  `item1` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '选项1（判断题时：正确；单选题/多选题时：选项A内容）',
  `item2` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '选项2（判断题时：错误；单选题/多选题时：选项B内容）',
  `item3` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '选项3（判断题为空；单选题/多选题时：选项C内容）',
  `item4` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '选项4（判断题为空；单选题/多选题时：选项D内容）',
  `explains` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '答案解析（错题展示时使用，支持长解析）',
  `url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '题目图片URL（可选，部分题目有配图，为空则无图片）',
  `subject` int NOT NULL COMMENT '科目类型（1=科目一，4=科目四）',
  `model` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '驾照类型（c1/c2/a1/a2/b1/b2等）',
  `question_type` int NOT NULL COMMENT '题型（1=判断题，2=单选题，3=多选题）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '数据创建时间（自动填充当前时间）',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '数据更新时间（自动更新）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_subject`(`subject` ASC) USING BTREE,
  INDEX `idx_model`(`model` ASC) USING BTREE,
  INDEX `idx_question_type`(`question_type` ASC) USING BTREE,
  INDEX `idx_subject_model`(`subject` ASC, `model` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8065 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '驾照题目表（科目一/科目四通用）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `user_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录账号（唯一）',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '' COMMENT '用户头像',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '邮箱（唯一）',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '' COMMENT '手机号（唯一）',
  `user_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'user' COMMENT '用户类型：user-普通用户',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `uk_email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '普通用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for verify_code
-- ----------------------------
DROP TABLE IF EXISTS `verify_code`;
CREATE TABLE `verify_code`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增ID',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收件人邮箱',
  `code` varchar(6) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '6位验证码',
  `expire_time` datetime NOT NULL COMMENT '验证码过期时间（1分钟）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（默认当前时间）',
  `used` tinyint NOT NULL DEFAULT 0 COMMENT '是否使用（0=未用，1=已用）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '验证码存储表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
