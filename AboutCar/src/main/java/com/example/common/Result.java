package com.example.common;

import lombok.Data;

@Data
public class Result {
    private int code;
    private String msg;
    private Object data;
    private Long timestamp;

    private Result() {
        this.timestamp = System.currentTimeMillis();
        this.code = 200;
        this.msg = "操作成功";
    }

    public static Result success() {
        return new Result();
    }

    public static Result success(Object data) {
        Result result = new Result();
        result.setData(data);
        return result;
    }

    public static Result success(Object data, String msg) {
        Result result = new Result();
        result.setCode(200);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }

    public static Result error(String msg) {
        Result result = new Result();
        result.setCode(500);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }

    public static Result error(String msg, Integer code) {
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(null);
        return result;
    }
}