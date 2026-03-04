package com.example.service.impl;

import com.example.common.PageResult;
import com.example.entity.Admin;
import com.example.mapper.AdminMapper;
import com.example.service.AdminService;
import com.example.utils.PasswordEncoderUtil; // 导入加密工具类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;


@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    /**
     * 查询所有管理员
     */
    @Override
    public List<Admin> findAll() {
        // 直接调用Mapper查询，无复杂业务逻辑
        return adminMapper.findAll();
    }

    /**
     * 条件查询管理员
     */
    @Override
    public List<Admin> findListByCondition(String username, String realName, Long roleId) {
        return adminMapper.findListByCondition(username, realName, roleId);
    }

    /**
     * 根据ID查询单个管理员
     */
    @Override
    public Admin findById(Long adminId) {
        // 业务校验：ID不能为空且大于0（Controller已校验，此处做双重保障）
        if (adminId == null || adminId <= 0) {
            throw new IllegalArgumentException("管理员ID必须大于0");
        }
        return adminMapper.findById(adminId);
    }

    /**
     * 新增管理员（含业务校验 + 密码加密）
     */
    @Override
    public void insert(Admin admin) {
        // 1. 业务参数校验（核心：避免无效数据入库）
        if (admin == null) {
            throw new IllegalArgumentException("管理员信息不能为空");
        }
        if (!StringUtils.hasText(admin.getUsername())) {
            throw new IllegalArgumentException("管理员账号不能为空");
        }
        if (!StringUtils.hasText(admin.getPassword())) {
            throw new IllegalArgumentException("管理员密码不能为空");
        }
        if (admin.getRoleId() == null || admin.getRoleId() <= 0) {
            throw new IllegalArgumentException("角色ID必须大于0");
        }

        // 2. 可选：校验账号唯一性（避免重复入库）
        List<Admin> existAdmin = adminMapper.findListByCondition(admin.getUsername(), null, null);
        if (!existAdmin.isEmpty()) {
            throw new RuntimeException("账号" + admin.getUsername() + "已存在，无法新增");
        }

        // ========== 核心修改：加密明文密码 ==========
        String encodedPwd = PasswordEncoderUtil.encodePassword(admin.getPassword());
        admin.setPassword(encodedPwd); // 替换为加密后的密码

        // 3. 调用Mapper新增
        adminMapper.insert(admin);
    }

    /**
     * 分页
     */
    @Override
    public PageResult<Admin> findAdminByPage(String username, String realName, Long roleId, Integer pageNum, Integer pageSize) {
        // 1. 分页参数校验 & 默认值（防止空值/非法值导致报错）
        pageNum = (pageNum == null || pageNum < 1) ? 1 : pageNum; // 页码最小为1
        pageSize = (pageSize == null || pageSize < 1 || pageSize > 100) ? 10 : pageSize; // 每页条数限制1-100

        // 2. 复用已有条件查询逻辑，获取所有符合条件的数据
        List<Admin> allList = adminMapper.findListByCondition(username, realName, roleId);
        if (allList == null) {
            allList = new ArrayList<>(); // 避免空指针异常
        }

        // 3. 纯 Java 分页核心逻辑（仅5行，原生 API 实现）
        int totalCount = allList.size(); // 总数据条数
        int startIndex = (pageNum - 1) * pageSize; // 分页起始索引（核心公式）
        int endIndex = Math.min(startIndex + pageSize, totalCount); // 结束索引（防止越界）
        // 截取当前页数据：如果起始索引超过总条数，返回空列表
        List<Admin> pageList = startIndex >= totalCount ? new ArrayList<>() : allList.subList(startIndex, endIndex);

        // 4. 封装分页结果返回
        return new PageResult<>(pageNum, pageSize, (long) totalCount, pageList);
    }

    /**
     * 更新管理员（含业务校验 + 密码加密（若修改密码））
     */
    @Override
    public void updateById(Admin admin) {
        // 1. 基础校验
        if (admin == null || admin.getAdminId() == null || admin.getAdminId() <= 0) {
            throw new IllegalArgumentException("管理员ID必须大于0");
        }

        // 2. 校验管理员是否存在
        Admin existAdmin = adminMapper.findById(admin.getAdminId());
        if (existAdmin == null) {
            throw new RuntimeException("要更新的管理员不存在（ID：" + admin.getAdminId() + "）");
        }

        // 3. 可选：更新时校验账号唯一性（排除自身）
        if (StringUtils.hasText(admin.getUsername())) {
            List<Admin> existList = adminMapper.findListByCondition(admin.getUsername(), null, null);
            // 过滤掉自身ID，判断是否有其他管理员使用该账号
            boolean isUsernameDuplicate = existList.stream()
                    .anyMatch(item -> !item.getAdminId().equals(admin.getAdminId()));
            if (isUsernameDuplicate) {
                throw new RuntimeException("账号" + admin.getUsername() + "已被其他管理员使用");
            }
        }

        // ========== 核心修改：密码加密（若传入新密码） ==========
        if (StringUtils.hasText(admin.getPassword())) {
            // 传入新密码：加密后更新
            String encodedNewPwd = PasswordEncoderUtil.encodePassword(admin.getPassword());
            admin.setPassword(encodedNewPwd);
        } else {
            // 未传入新密码：保留原有加密密码，避免更新后密码为空
            admin.setPassword(existAdmin.getPassword());
        }

        // 4. 调用Mapper更新
        adminMapper.updateById(admin);
    }

    /**
     * 根据ID删除管理员
     */
    @Override
    public void deleteById(Long adminId) {
        // 1. 基础校验
        if (adminId == null || adminId <= 0) {
            throw new IllegalArgumentException("管理员ID必须大于0");
        }

        // 2. 校验管理员是否存在
        Admin existAdmin = adminMapper.findById(adminId);
        if (existAdmin == null) {
            throw new RuntimeException("要删除的管理员不存在（ID：" + adminId + "）");
        }

        // 3. 调用Mapper删除
        adminMapper.deleteById(adminId);
    }

    @Override
    public boolean existsByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("待校验的邮箱不能为空");
        }
        List<Admin> adminList = adminMapper.findByEmail(email);
        return !CollectionUtils.isEmpty(adminList);
    }


    @Override
    public List<Admin> findByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("查询邮箱不能为空");
        }
        return adminMapper.findByEmail(email);
    }

    @Override
    public List<Admin> findByUsername(String username) {
        // 调用Mapper层方法，传递用户名参数
        return adminMapper.findByUsername(username);
    }

}