package com.ch503j.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;  // 错误码
    private final String message; // 错误信息

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message) {
        super(message);
        this.code = 500;  // 默认错误码
        this.message = message;
    }
}