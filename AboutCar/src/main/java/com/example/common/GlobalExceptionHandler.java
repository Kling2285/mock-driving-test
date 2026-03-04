package com.example.common;

import com.example.Exception.CustomException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice(basePackages = "com.example.controller")
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result exceptionHandler(Exception e) {
        logger.error("系统异常", e);
        return Result.error("系统异常", 500);
    }

    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public Result customExceptionHandler(CustomException e) {
        logger.error("自定义异常：{}", e.getMessage(), e);
        return Result.error(e.getMessage(), e.getCode());
    }
}