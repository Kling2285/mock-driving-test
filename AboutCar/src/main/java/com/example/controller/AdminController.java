package com.example.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.example.common.PageResult;
import com.example.common.Result; // 复用你已有的Result类
import com.example.entity.Admin;
import com.example.service.AdminService;
import com.example.utils.ExcelUtils; // 复用你已有的Excel工具类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/admin")
@ResponseBody
public class AdminController {

    @Autowired
    private AdminService adminService;

    // 1. 查询所有管理员（无分页）
    @GetMapping("/list")
    public Result list() {
        try {
            List<Admin> list = adminService.findAll();
            return Result.success(list); // 复用你的Result.success()
        } catch (Exception e) {
            return Result.error("查询所有管理员失败：" + e.getMessage()); // 复用你的Result.error()
        }
    }


    @GetMapping("/listByCondition")
    public Result listByCondition(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "realName", required = false) String realName,
            @RequestParam(value = "roleId", required = false) Long roleId
    ) {
        try {
            List<Admin> list = adminService.findListByCondition(username, realName, roleId);
            return Result.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("条件查询管理员失败：" + e.getMessage());
        }
    }

    @PostMapping
    public Result add(@RequestBody Admin admin) {
        try {
            if (!StringUtils.hasText(admin.getUsername())) {
                return Result.error("管理员账号不能为空");
            }
            if (!StringUtils.hasText(admin.getPassword())) {
                return Result.error("管理员密码不能为空");
            }
            if (admin.getRoleId() == null) {
                return Result.error("角色ID不能为空");
            }

            adminService.insert(admin);
            return Result.success(admin);
        } catch (Exception e) {
            return Result.error("新增管理员失败：" + e.getMessage());
        }
    }

    // 4. 根据ID查询单个管理员（路径传参）
    @GetMapping("/{adminId}")
    public Result findOne(@PathVariable("adminId") Long adminId) {
        try {
            if (adminId == null || adminId <= 0) {
                return Result.error("管理员ID必须大于0");
            }

            Admin admin = adminService.findById(adminId);
            if (admin == null) {
                return Result.error("未找到ID为" + adminId + "的管理员");
            }
            return Result.success(admin);
        } catch (Exception e) {
            return Result.error("查询管理员失败：" + e.getMessage());
        }
    }

    // 5. 更新管理员
    @PutMapping
    public Result update(@RequestBody Admin admin) {
        try {
            // 参数校验
            if (admin.getAdminId() == null || admin.getAdminId() <= 0) {
                return Result.error("管理员ID必须大于0");
            }
            // 先校验管理员是否存在
            Admin existAdmin = adminService.findById(admin.getAdminId());
            if (existAdmin == null) {
                return Result.error("要更新的管理员不存在");
            }

            adminService.updateById(admin);
            return Result.success(admin);
        } catch (Exception e) {
            return Result.error("更新管理员失败：" + e.getMessage());
        }
    }

    // 6. 根据ID删除管理员
    @DeleteMapping("/{adminId}")
    public Result delete(@PathVariable("adminId") Long adminId) {
        try {
            if (adminId == null || adminId <= 0) {
                return Result.error("管理员ID必须大于0");
            }
            Admin existAdmin = adminService.findById(adminId);
            if (existAdmin == null) {
                return Result.error("要删除的管理员不存在");
            }

            adminService.deleteById(adminId);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除管理员失败：" + e.getMessage());
        }
    }

    // 6.分页
    @GetMapping("/page")
    public Result findAdminByPage(
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "realName", required = false) String realName,
            @RequestParam(value = "roleId", required = false) Long roleId,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize
    ) {
        try {
            PageResult<Admin> pageResult = adminService.findAdminByPage(username, realName, roleId, pageNum, pageSize);
            return Result.success(pageResult);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("分页查询管理员失败：" + e.getMessage());
        }
    }

// 7. 导出Excel（带条件筛选）
    @GetMapping("/export")
    public void export(HttpServletResponse response,
                       @RequestParam(value = "username", required = false) String username,
                       @RequestParam(value = "realName", required = false) String realName,
                       @RequestParam(value = "roleId", required = false) Long roleId) throws IOException {
        try {
            // 条件查询要导出的数据
            List<Admin> list = adminService.findListByCondition(username, realName, roleId);
            ExcelUtils.export(response, list, "管理员列表");
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(Result.error("导出Excel失败：" + e.getMessage()).toString());
        }
    }

    // 8. 导入Excel（POST + 文件上传）
    @PostMapping("/import")
    public Result importExcel(@RequestParam("file") MultipartFile file) {
        try {
            // 参数校验：文件不能为空
            if (file.isEmpty()) {
                return Result.error("上传的Excel文件不能为空");
            }
            // 校验文件类型（可选）
            String originalFilename = file.getOriginalFilename();
            if (!originalFilename.endsWith(".xlsx") && !originalFilename.endsWith(".xls")) {
                return Result.error("仅支持.xlsx/.xls格式的Excel文件");
            }

            // 读取Excel文件（复用Hutool）
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
            List<Admin> list = reader.readAll(Admin.class);

            // 批量导入
            Integer count = 0;
            for (Admin item : list) {
                // 清空自增ID，避免主键冲突
                item.setAdminId(null);
                // 简单校验必填字段
                if (StringUtils.hasText(item.getUsername()) && StringUtils.hasText(item.getPassword())) {
                    adminService.insert(item);
                    count++;
                }
            }

            // 返回导入成功的条数
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("count", count);
            resultMap.put("total", list.size()); // 总读取条数
            return Result.success(resultMap);
        } catch (Exception e) {
            return Result.error("导入Excel失败：" + e.getMessage());
        }
    }
}
