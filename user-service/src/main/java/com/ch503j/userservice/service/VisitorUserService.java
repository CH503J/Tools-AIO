package com.ch503j.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ch503j.userservice.pojo.entity.VisitorUser;
import com.ch503j.userservice.pojo.vo.VisitorUserVO;
import jakarta.servlet.http.HttpServletResponse;

public interface VisitorUserService extends IService<VisitorUser> {

    VisitorUserVO visitorLogin(HttpServletResponse response);
}
