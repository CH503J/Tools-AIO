package com.ch503j.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ch503j.userservice.pojo.dto.UserDTO;
import com.ch503j.userservice.pojo.entity.User;
import com.ch503j.userservice.pojo.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService extends IService<User> {

    /**
     * 游客登录接口
     *
     * @param request  HTTP请求对象，用于获取客户端信息
     * @param response HTTP响应对象，用于设置返回信息
     * @return BaseResponse<VisitorUserVO> 统一响应格式，包含游客用户信息
     */
    UserVO visitorLogin(HttpServletRequest request, HttpServletResponse response);

    UserVO register(UserDTO userDTO, HttpServletRequest request, HttpServletResponse response);

    UserVO login(UserDTO userDto, HttpServletRequest request, HttpServletResponse response);
}
