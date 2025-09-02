package com.ch503j.common.enums;

public enum AuthEnum {

    USER_NOT_EXIST("用户不存在"),
    USER_EXIST("用户已存在"),
    USER_NOT_LOGIN("用户未登录"),
    USER_NOT_REGISTER("用户未注册"),
    USER_NOT_ACTIVE("用户未激活"),
    USER_NOT_VERIFY("用户未验证"),
    USER_NOT_EXIST_OR_PASSWORD_ERROR("用户不存在或密码错误"),
    USER_NOT_EXIST_OR_PHONE_ERROR("用户不存在或手机号错误");

    private final String des;

    AuthEnum(String des) {
        this.des = des;
    }
}

