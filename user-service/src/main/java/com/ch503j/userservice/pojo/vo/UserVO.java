package com.ch503j.userservice.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class UserVO {

    private Long userId;

    private String username;

    private String phone;

    private String role;

    private Integer status;

    private String visitorId;
}
