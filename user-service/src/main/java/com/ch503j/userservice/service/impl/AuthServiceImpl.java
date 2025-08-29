package com.ch503j.userservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch503j.common.exception.BusinessException;
import com.ch503j.userservice.mapper.VisitorUserMapper;
import com.ch503j.userservice.pojo.entity.VisitorUser;
import com.ch503j.userservice.pojo.vo.VisitorUserVO;
import com.ch503j.userservice.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Service
@Slf4j
public class AuthServiceImpl extends ServiceImpl<VisitorUserMapper, VisitorUser> implements AuthService {
    @Override
    public VisitorUserVO visitorLogin(HttpServletRequest request, HttpServletResponse response) {
        // 1. 获取 visitorId
        String visitorId = getVisitorIdFromCookie(request);

        // 2. 获取 IP
        String ip = resolveClientIp(request);
        log.info("访问者 IP: {}", ip);

        // 3. 查库 or 新建
        VisitorUser visitorUser = findOrCreateVisitor(visitorId);

        // 4. 设置 / 刷新 cookie
        refreshVisitorCookie(response, visitorUser.getVisitorId());

        return new VisitorUserVO(visitorUser.getUserId(), visitorUser.getVisitorId());
    }

    /**
     * 从 Cookie 获取 visitorId
     */
    private String getVisitorIdFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> "visitorId".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * 解析客户端 IP
     */
    private String resolveClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP"
        };
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.contains(",") ? ip.split(",")[0].trim() : ip;
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * 查库或新建 VisitorUser
     */
    private VisitorUser findOrCreateVisitor(String visitorId) {
        if (visitorId != null) {
            VisitorUser visitorUser = lambdaQuery().eq(VisitorUser::getVisitorId, visitorId).one();
            if (visitorUser != null) {
                visitorUser.setLastSeen(LocalDateTime.now());
                updateById(visitorUser);
                return visitorUser;
            }
            // cookie 有但数据库没 → 抛业务异常 or 生成新访客
            log.warn("Cookie 中的 visitorId={} 在数据库不存在，重新生成", visitorId);
        }

        // 没 cookie 或查不到 → 生成新的
        String newVisitorId = UUID.randomUUID().toString();
        VisitorUser visitorUser = createVisitor(newVisitorId);
        if (!save(visitorUser)) {
            throw new BusinessException("保存访客信息失败");
        }
        return visitorUser;
    }

    /**
     * 刷新 Cookie
     */
    private void refreshVisitorCookie(HttpServletResponse response, String visitorId) {
        Cookie cookie = new Cookie("visitorId", visitorId);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(7 * 24 * 3600);
        response.addCookie(cookie);
    }

    // 封装创建 VisitorUser 的方法
    private VisitorUser createVisitor(String visitorId) {
        VisitorUser visitorUser = new VisitorUser();
        visitorUser.setVisitorId(visitorId);
        visitorUser.setCreateTime(LocalDateTime.now());
        visitorUser.setLastSeen(LocalDateTime.now());
        return visitorUser;
    }
}
