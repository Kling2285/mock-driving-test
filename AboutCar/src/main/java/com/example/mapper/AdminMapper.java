package com.example.mapper;

import com.example.entity.Admin;
import com.example.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminMapper {
    /**
     * 查询所有管理员
     * @return 管理员列表
     */
    List<Admin> findAll();

    /**
     * 根据ID查询管理员
     * @param id 管理员ID
     * @return 管理员实体
     */
    Admin findById(Long id);


    /**
     * 条件查询管理员（核心补充！对应Service/Controller的条件查询）
     * @param username 账号（模糊查询，可为null）
     * @param realName 真实姓名（模糊查询，可为null）
     * @param roleId 角色ID（精确查询，可为null）
     * @return 符合条件的管理员列表
     */
    List<Admin> findListByCondition(
            @Param("username") String username,  // 显式指定参数名
            @Param("realName") String realName,  // 显式指定参数名
            @Param("roleId") Long roleId         // 显式指定参数名
    );

    /**
     * 新增管理员
     * @param admin 管理员实体（含用户名、密码、角色ID等）
     * @return 受影响行数（1=成功，0=失败）
     */
    int insert(Admin admin);

    /**
     * 根据ID更新管理员
     * @param admin 管理员实体（含要更新的字段和adminId）
     * @return 受影响行数
     */
    int updateById(Admin admin);

    /**
     * 根据ID删除管理员
     * @param id 管理员ID
     * @return 受影响行数
     */
    int deleteById(Long id);


    /**
     * 根据邮箱查询
     */
    List<Admin> findByEmail(String email);

    /**
     * 按用户名查询管理员列表
     * @param username 管理员用户名
     * @return 匹配的管理员列表
     * 注：@Param注解用于指定SQL中的参数名，避免多参数时的歧义（单参数也可省略，建议加上更规范）
     */
    List<Admin> findByUsername(@Param("username") String username);
}