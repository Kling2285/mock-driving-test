package com.example;

import com.example.entity.User;
import com.example.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

@SpringJUnitConfig
@ContextConfiguration(locations = {
        "classpath:applicationContext.xml"
})
class UserTests {
    @Autowired
    private UserMapper userMapper;

    @Test
    void findAll() {
        List<User> list = userMapper.findAll();
        list.forEach(item-> {
            System.out.println(item.getUserId()+"\t"+item.getUsername());
        });
    }

    @Test
    void findById() {
        User user=userMapper.findById(1L);
        System.out.println(user);
    }

    @Test
    void insert() {
        User user = new User();
        user.setUsername("test_user01");  // 用户名
        user.setPassword("123456");     // 密码
        user.setNickname("测试用户01");    // 昵称
        user.setPhone("13800138003");   // 手机号（选填，仅为示例）
        user.setEmail("test_user01@example.com");

        int rows = userMapper.insert(user);

        System.out.println("影响行数：" + rows);
        if (rows > 0) {
            System.out.println("新增用户成功，用户ID：" + user.getUserId());
        } else {
            System.out.println("新增用户失败");
        }
    }

    @Test
    void update() {
        User user = new User();
        user.setUserId(4L);
        user.setUsername("update_test");
        user.setPassword("654321");
        user.setNickname("更新测试用户");
        user.setPhone("13900139000");
        user.setEmail("update@qq.com");

        int rows = userMapper.update(user);

        System.out.println("影响行数：" + rows);
        if (rows > 0) {
            System.out.println("用户更新成功");
        } else {
            System.out.println("用户更新失败");
        }
    }

    @Test
    void delete() {
        Long deleteId = 4L;
        int rows = userMapper.delete(deleteId);

        System.out.println("影响行数：" + rows);
        if (rows > 0) {
            System.out.println("用户删除成功");
        } else {
            System.out.println("用户删除失败");
        }
    }

    @Test
    void findByEmail() {
        String testEmail = "test_user05@example.com";
        List<User> userList = userMapper.findByEmail(testEmail);

        if (userList == null || userList.isEmpty()) {
            System.out.println("未查询到邮箱为 " + testEmail + " 的用户");
        } else {
            System.out.println("查询到邮箱为 " + testEmail + " 的用户信息：");
            for (User item : userList) {
                System.out.println(item.getUserId() + "\t" + item.getUsername() + "\t" + item.getEmail());
            }
        }
    }



}

