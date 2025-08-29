package com.ch503j.userservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch503j.userservice.mapper.VisitorUserMapper;
import com.ch503j.userservice.pojo.entity.VisitorUser;
import com.ch503j.userservice.pojo.vo.VisitorUserVO;
import com.ch503j.userservice.service.VisitorUserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VisitorUserServiceImpl extends ServiceImpl<VisitorUserMapper, VisitorUser> implements VisitorUserService {
    @Override
    public VisitorUserVO visitorLogin(HttpServletResponse response) {

        // 生成uuid
        String uuid = UUID.randomUUID().toString();

        VisitorUser visitorUser = new VisitorUser();
        visitorUser.setVisitorId(uuid);
        visitorUser.setCreateTime(LocalDateTime.now());
        visitorUser.setLastSeen(LocalDateTime.now());

        save(visitorUser);

        // 设置cookie
        Cookie cookie = new Cookie("visitorId", uuid);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(7 * 24 * 3600);
        response.addCookie(cookie);

        return new VisitorUserVO(visitorUser.getUserId(), uuid);
    }
}
