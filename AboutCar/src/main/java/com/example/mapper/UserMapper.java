package com.example.mapper;

import com.example.entity.Admin;
import com.example.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface UserMapper {
    /**
     * 查询所有
     * @return 管理员列表
     */
    List<User> findAll();

    /**
     * 根据id查询用户
     * @param id 用户id
     * @return 用户实体
     */
    User findById(Long id);

    /**
     * 条件查询管理员
     * @param userId 用户id（精确查询，可为null）
     * @param username 账号（模糊查询，可为null）
     * @param nickname 用户昵称（模糊查询，可为null）
     * @return 返回对应数据
     */
    List<User> findByCondition(
            @Param("userId") Long userId,
            @Param("username") String username,
            @Param("nickname") String nickname
    );

    /**
     * 新增用户
     * @param user 用户实体
     * @return 受影响的行数
     */
    int insert(User user);


    /**
     * 更新用户
     * @param user 用户id
     * @return 受影响的行数
     */
    int update(User user);

    /**
     * 删除用户
     * @param id 用户id
     * @return 受影响的行数
     */
    int delete(Long id);

    /**
     * 批量删除用户
     * @param userIdList 用户id
     * @return 受影响的行数
     */
    int batchDelete(@Param("userIdList") List<Long> userIdList);


    /**
     * 根据邮箱查询
     */
    List<User> findByEmail(String email);

    /**
     * 按用户名查询管理员列表
     * @param username 管理员用户名
     * @return 匹配的管理员列表
     * 注：@Param注解用于指定SQL中的参数名，避免多参数时的歧义（单参数也可省略，建议加上更规范）
     */
    List<User> findByUsername(@Param("username") String username);
}





