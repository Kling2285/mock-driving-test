package com.example.mapper;


import com.example.entity.VerifyCode;
import org.apache.ibatis.annotations.Param;

/**
 * 邮件验证码 Mapper
 * 对应实体：com.example.entity.VerifyCode
 * 对应表：verify_code
 */
public interface VerifyCodeMapper {

    // 1. 新增邮件验证码（存储验证码到数据库）
    int insertVerifyCode(VerifyCode verifyCode);

    // 2. 根据邮箱查询最新的 未使用且未过期 验证码（校验用）
    VerifyCode selectUnusedCodeByEmail(@Param("email") String email);

    // 3. 更新验证码使用状态（标记为已使用，防止重复使用）
    int updateCodeUsedStatus(@Param("id") Long id, @Param("used") Integer used);

    // 4. 检查邮箱是否存在（核心需求：通用判断，返回数量 > 0 即存在）
    int checkEmailExists(@Param("email") String email);
}