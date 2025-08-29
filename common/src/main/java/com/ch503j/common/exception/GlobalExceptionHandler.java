package com.ch503j.common.exception;

import com.ch503j.common.pojo.dto.BaseResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> handleBusinessException(BusinessException e) {
        log.error("业务异常: {}", e.getMessage());
        return BaseResponse.fail(e.getCode(), e.getMessage());
    }

    /**
     * 处理空指针异常
     */
    @ExceptionHandler(NullPointerException.class)
    public BaseResponse<?> handleNullPointerException(NullPointerException e) {
        log.error("空指针异常: ", e);
        return BaseResponse.fail(500, "空指针异常，请联系管理员");
    }

    /**
     * 处理所有其它异常
     */
    @ExceptionHandler(Exception.class)
    public BaseResponse<?> handleException(Exception e) {
        log.error("系统异常: ", e);
        return BaseResponse.fail(500, "系统开小差了，请稍后再试");
    }
}