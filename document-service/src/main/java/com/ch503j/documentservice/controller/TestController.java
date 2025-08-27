package com.ch503j.documentservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/document")
public class TestController {

    @GetMapping("/test")
    public String test(){
        return "文档处理服务测试接口";
    }
}
