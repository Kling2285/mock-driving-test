package com.example.service.impl;

import com.example.common.PageResult;
import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.service.UserService;
import com.example.utils.PasswordEncoderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    // 你的其他原有方法（findAll、findById、insert等）保持不变，无需修改
    // ...（此处省略你原有代码，仅修改密码方法）...

    /**
     * 修改密码核心逻辑：完全复用你的现有Mapper（findById + update），无selectById、无updateById
     * 无DTO、无DAO，纯参数传递，适配你的原生MyBatis Mapper
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateUserPassword(Long userId, String oldPwd, String newPwd) {
        // 1. 复用你现有的 mapper.findById() 方法（替代 selectById）查询用户
        User dbUser = userMapper.findById(userId);
        if (dbUser == null) {
            return false; // 用户不存在，返回失败
        }

        // 2. 用你的 PasswordEncoderUtil 验证原密码（明文 vs 数据库加密密文）
        boolean isOldPwdMatch = PasswordEncoderUtil.matchPassword(oldPwd, dbUser.getPassword());
        if (!isOldPwdMatch) {
            return false; // 原密码错误，返回失败
        }

        // 3. 用你的 PasswordEncoderUtil 加密新密码
        String encodedNewPwd = PasswordEncoderUtil.encodePassword(newPwd);

        // 4. 给查询到的用户设置新密码（加密后的）
        dbUser.setPassword(encodedNewPwd);

        // 5. 复用你现有的 mapper.update() 方法（替代 updateById）更新密码
        int updateCount = userMapper.update(dbUser);

        // 6. 受影响行数>0即为修改成功
        return updateCount > 0;
    }

    // 你的其他原有方法保持不变...
    @Override
    public List<User> findAll() {
        return userMapper.findAll();
    }

    @Override
    public User findById(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }
        return userMapper.findById(userId);
    }

    @Override
    public List<User> findByCondition(Long userId, String username, String nickname) {
        return userMapper.findByCondition(userId, username, nickname);
    }

    @Override
    public boolean insert(User user) {
        if (user == null) {
            throw new IllegalArgumentException("用户信息不能为空");
        }
        if (!StringUtils.hasText(user.getUsername())) {
            throw new IllegalArgumentException("用户账号不能为空");
        }
        if (!StringUtils.hasText(user.getPassword())) {
            throw new IllegalArgumentException("用户密码不能为空");
        }
        if (!StringUtils.hasText(user.getEmail())) {
            throw new IllegalArgumentException("用户邮箱不能为空");
        }

        List<User> existUser = userMapper.findByCondition(null, user.getUsername(), null);
        if (!existUser.isEmpty()) {
            throw new RuntimeException("账号" + user.getUsername() + "已存在，无法新增");
        }

        String encodedPwd = PasswordEncoderUtil.encodePassword(user.getPassword());
        user.setPassword(encodedPwd);

        int rows = userMapper.insert(user);
        return rows > 0;
    }

    @Override
    public boolean update(User user) {
        if (user == null || user.getUserId() == null || user.getUserId() <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }

        User existUser = userMapper.findById(user.getUserId());
        if (existUser == null) {
            throw new RuntimeException("要更新的用户不存在（ID：" + user.getUserId() + "）");
        }

        if (StringUtils.hasText(user.getUsername())) {
            List<User> existList = userMapper.findByCondition(null, user.getUsername(), null);
            boolean isUsernameDuplicate = existList.stream()
                    .anyMatch(item -> !item.getUserId().equals(user.getUserId()));
            if (isUsernameDuplicate) {
                throw new RuntimeException("账号" + user.getUsername() + "已被其他用户使用");
            }
        }

        if (!StringUtils.hasText(user.getUserType())) {
            user.setUserType(existUser.getUserType() != null ? existUser.getUserType() : "user");
        }

        if (StringUtils.hasText(user.getPassword())) {
            String encodedNewPwd = PasswordEncoderUtil.encodePassword(user.getPassword());
            user.setPassword(encodedNewPwd);
        } else {
            user.setPassword(existUser.getPassword());
        }
        int rows = userMapper.update(user);
        return rows > 0;
    }

    @Override
    public boolean delete(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("用户ID必须大于0");
        }

        User existUser = userMapper.findById(userId);
        if (existUser == null) {
            throw new RuntimeException("要删除的用户不存在（ID：" + userId + "）");
        }

        int rows = userMapper.delete(userId);
        return rows > 0;
    }

    @Override
    public boolean batchDelete(List<Long> userIdList) {
        if (CollectionUtils.isEmpty(userIdList)) {
            throw new IllegalArgumentException("待删除的用户ID列表不能为空");
        }
        for (Long userId : userIdList) {
            if (userId == null || userId <= 0) {
                throw new IllegalArgumentException("用户ID必须大于0，无效ID：" + userId);
            }
            User existUser = userMapper.findById(userId);
            if (existUser == null) {
                throw new RuntimeException("要删除的用户不存在（ID：" + userId + "）");
            }
        }
        int rows = userMapper.batchDelete(userIdList);
        return rows > 0;
    }

    @Override
    public PageResult<User> findPageByCondition(Long userId, String username, String nickname, Integer pageNum, Integer pageSize) {
        pageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum;
        pageSize = (pageSize == null || pageSize < 1 || pageSize > 100) ? 10 : pageSize;

        List<User> allList = userMapper.findByCondition(userId, username, nickname);
        if (allList == null) {
            allList = new ArrayList<>();
        }

        int totalCount = allList.size();
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalCount);
        List<User> pageList = startIndex >= totalCount ? new ArrayList<>() : allList.subList(startIndex, endIndex);

        return new PageResult<>(pageNum, pageSize, (long) totalCount, pageList);
    }

    @Override
    public boolean existsByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("待校验的邮箱不能为空");
        }
        List<User> userList = userMapper.findByEmail(email);
        return !CollectionUtils.isEmpty(userList);
    }

    @Override
    public List<User> findByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("查询邮箱不能为空");
        }
        return userMapper.findByEmail(email);
    }

    @Override
    public List<User> findByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("查询用户名不能为空");
        }
        return userMapper.findByUsername(username);
    }
}