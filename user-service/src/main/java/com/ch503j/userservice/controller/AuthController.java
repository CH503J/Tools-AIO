package com.ch503j.userservice.controller;

import com.ch503j.common.pojo.dto.BaseResponse;
import com.ch503j.userservice.pojo.vo.VisitorUserVO;
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
    private AuthService visitorUserService;


    /**
     * 游客登录接口
     *
     * @param request  HTTP请求对象，用于获取客户端信息
     * @param response HTTP响应对象，用于设置返回信息
     * @return BaseResponse<VisitorUserVO> 统一响应格式，包含游客用户信息
     */
    @PostMapping("/visitorLogin")
    public BaseResponse<VisitorUserVO> visitorLogin(HttpServletRequest request, HttpServletResponse response) {
        return BaseResponse.success(visitorUserService.visitorLogin(request, response));
    }
}
