package com.example.utils;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExcelUtils {

    public static void export(HttpServletResponse response, List<?> list, String fileName) {
        // 空值校验：避免空指针
        if (list == null) {
            throw new IllegalArgumentException("导出数据列表不能为空");
        }
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "导出数据"; // 默认文件名
        }

        ExcelWriter writer = null;
        try {
            response.reset();

            writer = ExcelUtil.getWriter(true);
            writer.write(list, true);

            // 优化2：简化Content-Disposition，避免多文件名导致前端解析异常
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8"); // 新增：统一编码
            String encodeFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodeFileName + ".xlsx\"");

            // 禁用缓存（保留）
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);

            // 4. 输出流操作（保留）
            try (ServletOutputStream os = response.getOutputStream()) {
                writer.flush(os, true);
            }
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new RuntimeException("Excel导出失败：" + e.getMessage(), e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}