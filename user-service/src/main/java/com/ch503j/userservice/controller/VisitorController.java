package com.ch503j.userservice.controller;

import com.ch503j.common.pojo.dto.BaseResponse;
import com.ch503j.userservice.pojo.vo.VisitorUserVO;
import com.ch503j.userservice.service.VisitorUserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class VisitorController {

    @Resource
    private VisitorUserService visitorUserService;


    @GetMapping("/visitorLogin")
    public BaseResponse<VisitorUserVO> visitorLogin(HttpServletResponse response) {
        return BaseResponse.success(visitorUserService.visitorLogin(response));
    }
}
