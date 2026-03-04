package com.example.Exception;

import lombok.Getter;

public class CustomException extends RuntimeException {
    @Getter
    private int code;

    public CustomException(int code, String message) {
        super(message);
        this.code = code;
    }
}