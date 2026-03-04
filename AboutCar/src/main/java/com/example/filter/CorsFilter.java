package com.example.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;


public class CorsFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;

        // 1. 允许所有源（生产环境替换为你的前端域名，如http://localhost:5173）
        String origin = request.getHeader("Origin");
        if (origin != null) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        }

        // 2. 允许的请求方法
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

        // 3. 允许的请求头（核心修改：添加 token，补充到原有列表后）
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, token");

        // 4. 允许携带Cookie
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // 5. 预检请求缓存时间（减少OPTIONS请求）
        response.setHeader("Access-Control-Max-Age", "3600");

        // 6. 处理OPTIONS预检请求（直接返回200）
        if ("OPTIONS".equals(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // 继续执行过滤器链
        chain.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}