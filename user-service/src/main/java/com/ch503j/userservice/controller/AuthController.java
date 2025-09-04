package com.ch503j.userservice.controller;

import com.ch503j.common.pojo.dto.BaseResponse;
import com.ch503j.userservice.pojo.dto.UserDTO;
import com.ch503j.userservice.pojo.vo.UserVO;
import com.ch503j.userservice.service.AuthService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;


    /**
     * 游客登录接口
     *
     * @param request  HTTP请求对象，用于获取客户端信息
     * @param response HTTP响应对象，用于设置返回信息
     * @return BaseResponse<VisitorUserVO> 统一响应格式，包含游客用户信息
     */
    @PostMapping("/visitorLogin")
    public BaseResponse<UserVO> visitorLogin(HttpServletRequest request, HttpServletResponse response) {
        return BaseResponse.success(authService.visitorLogin(request, response));
    }

    /**
     * 用户注册接口
     *
     * @param userDto  用户注册信息传输对象，包含用户名、手机号、账号和密码
     * @param request  HTTP请求对象，用于获取客户端信息
     * @param response HTTP响应对象，用于设置返回信息
     * @return BaseResponse<UserVO> 统一响应格式，包含注册成功后的用户信息
     */
    @PostMapping("/register")
    public BaseResponse<UserVO> register(UserDTO userDto, HttpServletRequest request, HttpServletResponse response) {
        return BaseResponse.success(authService.register(userDto, request, response));
    }

    @PostMapping("/login")
    public BaseResponse<UserVO> login(UserDTO userDto, HttpServletRequest request, HttpServletResponse response){
        return BaseResponse.success(authService.login(userDto, request, response));
    }
}
