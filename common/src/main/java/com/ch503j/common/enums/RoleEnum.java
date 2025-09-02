package com.ch503j.common.enums;

public enum RoleEnum{
    USER("普通用户", "USER"),
    ADMIN("管理员", "ADMIN"),
    VISITOR("游客", "VISITOR");

    private final String des;
    private final String role;

    RoleEnum(String des, String role) {
        this.des = des;
        this.role = role;
    }
}
