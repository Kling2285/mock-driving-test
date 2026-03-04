package com.example.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.example.common.PageResult;
import com.example.common.Result;
import com.example.entity.User;
import com.example.service.UserService;
import com.example.utils.ExcelUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
@ResponseBody
public class UserController {

    @Autowired
    private UserService userService;

    //1.查询全部
    @GetMapping("/list")
    public Result list() {
        try{
            List<User> list = userService.findAll();
            return Result.success(list);
        }catch (Exception e){
            return Result.error("查询用户失败"+e.getMessage());
        }
    }

    //2.条件查询
    @GetMapping("/listByCondition")
    public Result listByCondition(
            @RequestParam(value = "userId" ,required = false) Long userId,
            @RequestParam(value="username",required = false) String username,
            @RequestParam(value = "nickname" ,required = false) String nickname
    ) {
        try{
            List<User> list = userService.findByCondition(userId, username, nickname);
            return Result.success(list);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("条件查询失败"+e.getMessage());
        }
    }

    //3.单个查询
    @GetMapping("/{userId}")
    public Result findById(@PathVariable("userId") Long userId) {
        try{
            if(userId==null||userId<=0){
                return Result.error("id无效");
            }
            User user = userService.findById(userId);
            if(user==null){
                return Result.error("未找到ID为"+userId+"的用户");
            }
            return Result.success(user);
        }catch (Exception e){
            return Result.error("单个查询失败"+e.getMessage());
        }
    }

    //4.添加用户
    @PostMapping
    public Result save(@RequestBody User user) {
        try{
            if(!StringUtils.hasText(user.getUsername())){
                return Result.error("用户名不为空");
            }
            if(!StringUtils.hasText(user.getPassword())){
                return  Result.error("用户密码不为空");
            }
            if(!StringUtils.hasText(user.getPhone())){
                return Result.error("用户电话不为空");
            }
            if(!StringUtils.hasText(user.getEmail())){
                return Result.error("用户邮箱不为空");
            }
            boolean success = userService.insert(user);
            if (!success) {
                return Result.error("新增用户失败：数据库操作无影响");
            }
            return Result.success(user);
        }catch (Exception e){
            return Result.error("新增用户失败"+e.getMessage());
        }
    }

    //5.修改用户
    @PutMapping
    public Result update(@RequestBody User user) {
        try{
            if(user.getUserId()==null||user.getUserId()<=0){
                return Result.error("用户id无效");
            }
            User existUser = userService.findById(user.getUserId());
            if(existUser==null){
                return Result.error("该用户不存在");
            }
            boolean success = userService.update(user);
            if (!success) {
                return Result.error("更新用户失败：数据库操作无影响");
            }
            User updatedUser = userService.findById(user.getUserId());
            return Result.success(updatedUser);
        }catch (Exception e){
            return Result.error("更新用户失败"+e.getMessage());
        }
    }

    //6.删除用户
    @DeleteMapping("/single/{userId}")
    public Result delete(@PathVariable("userId") Long userId) {
        try {
            if(userId==null||userId<=0){
                return Result.error("用户id无效");
            }
            User existUser = userService.findById(userId);
            if(existUser==null){
                return Result.error("该用户不存在");
            }
            boolean success = userService.delete(userId);
            if (!success) {
                return Result.error("删除用户失败：数据库操作无影响");
            }
            return Result.success(existUser);
        }catch (Exception e){
            return Result.error("删除失败"+e.getMessage());
        }
    }

    @DeleteMapping("/batch")
    public Result batchDelete(@RequestBody List<Long> userIdList) {
        try {
            boolean success = userService.batchDelete(userIdList);
            if (!success) {
                return Result.error("批量删除用户失败：数据库操作无影响");
            }
            return Result.success("批量删除成功，共删除" + userIdList.size() + "条用户数据");
        } catch (Exception e) {
            return Result.error("批量删除用户失败：" + e.getMessage());
        }
    }

    //7.导出用户数据
    @GetMapping("/export")
    public void export(HttpServletResponse response,
                       @RequestParam(value = "userId" ,required = false) Long userId,
                       @RequestParam(value="username",required = false) String username,
                       @RequestParam(value = "nickname" ,required = false) String nickname) throws IOException {
        try {
            List<User> list = userService.findByCondition(userId, username, nickname);
            ExcelUtils.export(response,list,"用户列表");
        }catch (Exception e){
            e.printStackTrace();
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(Result.error("导出失败"+e.getMessage()).toString());
        }
    }

    //8.导入用户数据
    @PostMapping("/import")
    public Result importExcel(@RequestParam("file") MultipartFile file) {
        try{
            if(file.isEmpty()){
                // 修复：补充具体提示
                return Result.error("导入失败：文件不能为空");
            }
            String originalFilename = file.getOriginalFilename();
            if(!originalFilename.endsWith(".xlsx")&&!originalFilename.endsWith(".xls")){
                // 修复：补充具体提示
                return Result.error("导入失败：仅支持.xlsx/.xls格式文件");
            }
            ExcelReader reader = ExcelUtil.getReader(file.getInputStream());
            List<User> list=reader.readAll(User.class);

            Integer count=0;
            for(User user:list){
                user.setUserId(null);
                if(StringUtils.hasText(user.getUsername())){
                    userService.insert(user);
                    count++;
                }
            }
            Map<String,Object> resultMap=new HashMap<>();
            resultMap.put("count",count);
            resultMap.put("total",list.size());
            return Result.success(resultMap);
        }catch (Exception e){
            return Result.error("导入失败"+e.getMessage());
        }
    }

    //9.分页
    @GetMapping("/page")
    public Result page(
            @RequestParam(value = "userId" ,required = false) Long userId,
            @RequestParam(value="username",required = false) String username,
            @RequestParam(value = "nickname" ,required = false) String nickname,
            @RequestParam(value = "pageNum", required = false) Integer pageNum,
            @RequestParam(value = "pageSize", required = false) Integer pageSize
    ){
        try{
            PageResult<User> pageResult=userService.findPageByCondition(userId, username, nickname, pageNum, pageSize);
            return Result.success(pageResult);
        }catch (Exception e){
            e.printStackTrace();
            return Result.error("分页查询失败"+e.getMessage());
        }
    }

    /**
     * 修改密码接口：直接用Map接收前端参数，无需DTO，无DAO
     */
    @PostMapping("/updatePassword")
    public Result updateUserPassword(@RequestBody Map<String, Object> paramMap) {
        Long userId = Long.parseLong(paramMap.get("userId").toString());
        String oldPwd = paramMap.get("oldPwd").toString();
        String newPwd = paramMap.get("newPwd").toString();

        // 调用Service层方法
        boolean isSuccess = userService.updateUserPassword(userId, oldPwd, newPwd);
        if (isSuccess) {
            return Result.success("密码修改成功");
        } else {
            return Result.error("原有密码错误或用户不存在，修改失败");
        }
    }


}
