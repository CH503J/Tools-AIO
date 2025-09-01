package com.ch503j.userservice.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserVO {

    private String username;

    private String phone;

    private String role;

    private Integer status;

    private String visitorId;
}
