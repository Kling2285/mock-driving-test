package com.example;

import com.example.mapper.AdminMapper;
import com.example.entity.Admin;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

// 核心注解1：指定 Spring 配置文件路径（加载上下文）
@SpringJUnitConfig // Junit5 适配 Spring 测试的核心注解
@ContextConfiguration(locations = {
        "classpath:applicationContext.xml"
})
class SpringWorkApplicationTests {

    @Autowired
    private AdminMapper adminMapper;

    @Test
    void testAdminMapper() {
        List<Admin> list = adminMapper.findAll();
        list.forEach(item-> {
            System.out.println(item.getAdminId()+"\t"+item.getUsername());
        });
    }

    @Test
    void findByIdMapper() {
        Admin admin=adminMapper.findById(1L);
        System.out.println(admin);
    }
    @Test
    void testInsert() {
        // 1. 构建测试管理员对象
        Admin admin = new Admin();
        admin.setUsername("test_admin_insert"); // 唯一账号
        admin.setPassword("$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2"); // 加密密码123456
        admin.setEmail("test_insert@example.com"); // 唯一邮箱
        admin.setPhone("13800138007"); // 唯一手机号
        admin.setRoleId(1L); // 角色ID（需确保角色表有此ID，无则改1）
        admin.setRealName("测试新增管理员");

        // 2. 执行新增
        int insertCount = adminMapper.insert(admin);

        // 3. 校验结果
        System.out.println("===== 测试 insert 方法 =====");
        System.out.println("新增受影响行数：" + insertCount);
        System.out.println("新增后返回的管理员ID：" + admin.getAdminId()); // 自增ID
        // 验证新增是否成功（行数=1则成功）
        assert insertCount == 1 : "新增管理员失败！受影响行数不为1";
    }

    @Test
    void testUpdateById() {
        Admin updateAdmin = new Admin();

        Long targetAdminId = 2L;
        updateAdmin.setAdminId(targetAdminId);

        // 3. 设置要更新的字段
        updateAdmin.setUsername("test_admin_update_new");
        updateAdmin.setPassword("$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2");
        updateAdmin.setEmail("test_update_new@example.com");
        updateAdmin.setPhone("13800138009");
        updateAdmin.setRoleId(2L);
        updateAdmin.setRealName("更新后管理员");

        // 4. 先校验：目标ID是否存在（避免更新不存在的数据）
        Admin beforeUpdate = adminMapper.findById(targetAdminId);
        if (beforeUpdate == null) {
            System.out.println("报错：数据库中没有 ID=" + targetAdminId + " 的管理员！");
            return; // 提前退出，避免后续报错
        }

        // 5. 执行更新
        int updateCount = adminMapper.updateById(updateAdmin);

        // 6. 查询更新后的结果
        Admin afterUpdate = adminMapper.findById(targetAdminId); // 修复：用定义好的 targetAdminId，而非未定义的 adminId

        // 7. 打印结果 + 校验
        System.out.println("===== 测试 updateById 方法 =====");
        System.out.println("更新受影响行数：" + updateCount);
        if (afterUpdate != null) {
            System.out.println("更新后管理员姓名：" + afterUpdate.getRealName());
            // 验证更新成功
            assert updateCount == 1 : "更新管理员失败！受影响行数不为1（可能ID不存在或数据无变化）";
            assert "更新后管理员".equals(afterUpdate.getRealName()) : "管理员姓名更新失败！";
        } else {
            System.out.println("报错：更新后查询不到 ID=" + targetAdminId + " 的管理员！");
        }
    }

    // 测试删除管理员（deleteById 方法）
    @Test
    void testDeleteById() {
        Long targetAdminId = 2L;

        Admin beforeDelete = adminMapper.findById(targetAdminId);
        if (beforeDelete == null) {
            System.out.println("  提示：数据库中没有 ID=" + targetAdminId + " 的管理员，删除操作跳过！");
            return;
        }

        // 3. 执行删除（直接传固定ID 2L）
        int deleteCount = adminMapper.deleteById(targetAdminId);

        // 4. 查询删除后的结果
        Admin afterDelete = adminMapper.findById(targetAdminId);

        // 5. 打印结果 + 校验
        System.out.println("===== 测试 deleteById 方法 =====");
        System.out.println("删除受影响行数：" + deleteCount);
        System.out.println("删除后查询结果：" + afterDelete); // 正常应为 null

        // 6. 验证删除是否成功
        assert deleteCount == 1 : " 删除管理员失败！受影响行数不为1（可能ID不存在）";
        assert afterDelete == null : "管理员未被成功删除！删除后仍能查询到数据";

        System.out.println("删除 ID=" + targetAdminId + " 的管理员成功！");
    }


    @Test
    void findByEmail() {
        String testEmail = "admin@example.com";

        List<Admin> adminList = adminMapper.findByEmail(testEmail);

        if (adminList == null || adminList.isEmpty()) {
            System.out.println("未查询到邮箱为 " + testEmail + " 的管理员");
        } else {
            System.out.println("查询到邮箱为 " + testEmail + " 的管理员信息：");
            for (Admin item : adminList) {
                // 修复2：补充完整的属性获取方法（item.getAdminId()，而非item.get）
                System.out.println(item.getAdminId() + "\t" + item.getUsername() + "\t" + item.getEmail());
            }
        }
    }

}
