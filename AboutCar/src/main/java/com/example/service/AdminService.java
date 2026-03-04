package com.example.service;

import com.example.common.PageResult;
import com.example.entity.Admin;

import java.util.List;

public interface AdminService {

    /**
     * 查询所有管理员
     */
    List<Admin> findAll();

    /**
     * 条件查询管理员
     * @param username 账号（模糊查询）
     * @param realName 真实姓名（模糊查询）
     * @param roleId 角色ID（精确查询）
     */
    List<Admin> findListByCondition(String username, String realName, Long roleId);

    /**
     * 根据ID查询单个管理员
     * @param adminId 管理员ID
     */
    Admin findById(Long adminId);

    /**
     * 新增管理员
     * @param admin 管理员实体
     */
    void insert(Admin admin);

    /**
     * 更新管理员
     * @param admin 管理员实体（必须包含adminId）
     */
    void updateById(Admin admin);

    /**
     * 根据ID删除管理员
     * @param adminId 管理员ID
     */
    void deleteById(Long adminId);

    /**
     * 分页
     */
    PageResult<Admin> findAdminByPage(String username, String realName, Long roleId, Integer pageNum, Integer pageSize);

    /**
     * 邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 邮箱查找
     */
    List<Admin> findByEmail(String email);

    /**
     * 按用户名查询管理员列表
     * @param username 管理员用户名
     * @return 匹配的管理员列表（用户名可能唯一，实际返回单个或空列表）
     */
    List<Admin> findByUsername(String username);
}