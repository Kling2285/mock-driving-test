package com.example.service;

import com.example.common.PageResult;
import com.example.entity.User;

import java.util.List;

public interface UserService {
    List<User> findAll();

    User findById(Long userId);

    List<User> findByCondition(Long userId, String username, String nickname); // 调整参数顺序和Mapper一致

    boolean insert(User user);

    boolean update(User user);

    boolean delete(Long userId);

    PageResult<User> findPageByCondition(Long userId, String username, String nickname,Integer pageNum, Integer pageSize);

    boolean batchDelete(List<Long> userIdList);

    boolean existsByEmail(String email);

    List<User> findByEmail(String email);

    List<User> findByUsername(String username);

    /**
     * 修改用户密码：直接传入三个参数，无需DTO
     * @param userId 用户ID
     * @param oldPwd 原有明文密码
     * @param newPwd 新明文密码
     * @return 是否修改成功
     */
    boolean updateUserPassword(Long userId, String oldPwd, String newPwd);
}