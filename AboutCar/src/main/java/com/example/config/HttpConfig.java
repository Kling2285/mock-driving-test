package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.time.Duration;

@Configuration
public class HttpConfig {

    // 配置RestTemplate，设置超时时间，全局注入使用
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000); // 连接超时：5秒
        factory.setReadTimeout(30000); // 读取超时：30秒（足够接收全部题目数据）
        return new RestTemplate(factory);
    }
}