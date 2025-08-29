package com.ch503j.common.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 状态码：0=成功，非0=失败 */
    private int code;

    /** 返回提示信息 */
    private String message;

    /** 返回数据，可以是任何类型 */
    private T data;

    // ----------------- 成功方法 -----------------

    public static <T> BaseResponse<T> success() {
        return new BaseResponse<>(0, "操作成功", null);
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, "操作成功", data);
    }

    public static <T> BaseResponse<T> success(String message, T data) {
        return new BaseResponse<>(0, message, data);
    }

    // ----------------- 失败方法 -----------------

    public static <T> BaseResponse<T> fail() {
        return new BaseResponse<>(500, "操作失败", null);
    }

    public static <T> BaseResponse<T> fail(String message) {
        return new BaseResponse<>(500, message, null);
    }

    public static <T> BaseResponse<T> fail(int code, String message) {
        return new BaseResponse<>(code, message, null);
    }

    public static <T> BaseResponse<T> fail(int code, String message, T data) {
        return new BaseResponse<>(code, message, data);
    }
}
