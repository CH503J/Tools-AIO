package com.ch503j.imagewatermark.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/watermark")
public class TestController {

    @GetMapping("/test")
    public String test(){
        return "图片加水印服务测试接口";
    }
}
