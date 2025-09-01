package com.ch503j.userservice.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 2, max = 10, message = "用户名长度必须在2到10之间")
    private String username;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "账号不能为空")
    @Size(min = 4, max = 20, message = "账号长度必须在4-20之间")
    private String userId;

    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 20, message = "密码长度必须在8-20之间")
    private String password;
}
