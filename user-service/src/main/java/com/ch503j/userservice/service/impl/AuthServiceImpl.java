package com.ch503j.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ch503j.common.enums.RoleEnum;
import com.ch503j.common.exception.BusinessException;
import com.ch503j.userservice.mapper.UserMapper;
import com.ch503j.userservice.pojo.dto.UserDTO;
import com.ch503j.userservice.pojo.entity.User;
import com.ch503j.userservice.pojo.vo.UserVO;
import com.ch503j.userservice.service.AuthService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Service
@Slf4j
public class AuthServiceImpl extends ServiceImpl<UserMapper, User> implements AuthService {

    @Resource
    private UserMapper userMapper;


    @Override
    public UserVO visitorLogin(HttpServletRequest request, HttpServletResponse response) {
        // 1. 获取 visitorId
        String visitorId = getVisitorIdFromCookie(request);

        // 2. 获取客户端 IP
        String ip = resolveClientIp(request);
        log.info("访问者 IP: {}", ip);

        // 3. 查库或新建
        User user = findOrCreateVisitor(visitorId, ip);

        // 4. 设置/刷新 cookie
        refreshVisitorCookie(response, user.getVisitorId());

        UserVO userVO = new UserVO();
        userVO.setUserId(user.getUserId());
        userVO.setVisitorId(user.getVisitorId());
        userVO.setUsername(user.getUsername());
        userVO.setRole(user.getRole());
        userVO.setStatus(user.getStatus());

        // 5. 返回 UserVO
        return userVO;
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
        String[] headers = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.contains(",") ? ip.split(",")[0].trim() : ip;
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * 查库或新建游客用户
     */
    private User findOrCreateVisitor(String visitorId, String ip) {
        User user;

        if (visitorId != null) {
            user = lambdaQuery().eq(User::getVisitorId, visitorId).one();
            if (user != null) {
                user.setLastSeen(LocalDateTime.now());
                user.setLastIp(ip); // 更新最新 IP
                updateById(user);
                return user;
            }
            log.warn("Cookie 中 visitorId={} 在数据库不存在，将重新生成", visitorId);
        }

        // 没 cookie 或查不到 → 生成新的游客账号
        String newVisitorId = UUID.randomUUID().toString();
        User visitorUser = createVisitor(newVisitorId, ip);

        if (!save(visitorUser)) {
            throw new BusinessException("保存游客信息失败");
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
        cookie.setMaxAge(7 * 24 * 3600); // 7天
        response.addCookie(cookie);
    }

    /**
     * 封装创建游客用户的方法
     */
    private User createVisitor(String visitorId, String ip) {
        User user = new User();
        user.setVisitorId(visitorId);
        user.setRole(RoleEnum.VISITOR.getRole());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setLastSeen(LocalDateTime.now());
        user.setLastIp(ip);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO register(UserDTO userDTO, HttpServletRequest request, HttpServletResponse response) {
        String visitorId = getVisitorIdFromCookie(request);
        if (!StringUtils.hasText(visitorId)) {
            throw new BusinessException("visitorId 缺失");
        }

        // 根据 visitorId 查已有游客
        User user = userMapper.selectOne(new QueryWrapper<User>()
                .eq("visitor_id", visitorId));

        String ip = resolveClientIp(request);

        try {
            if (user != null) {
                // ====== 路径1：已有游客 → 升级/完善资料 ======
                // 先做唯一性校验：同表中是否已有相同 user_id / visitor_id（排除当前这条）
                hasUniqueUser(userDTO.getUserId(), visitorId, userDTO.getPhone(), user.getId());

                // 更新字段（注意别覆盖主键/创建时间/visitorId）
                if (userDTO.getUserId() != null) user.setUserId(userDTO.getUserId());
                if (StringUtils.hasText(userDTO.getUsername())) user.setUsername(userDTO.getUsername());
                if (StringUtils.hasText(userDTO.getPhone())) user.setPhone(userDTO.getPhone());
                if (StringUtils.hasText(userDTO.getPassword())) user.setPassword(userDTO.getPassword());
                user.setRole(RoleEnum.USER.name()); // 升级为正式用户
                user.setUpdateTime(LocalDateTime.now());
                user.setLastIp(ip);
                user.setLastSeen(LocalDateTime.now());

                userMapper.updateById(user);
            } else {
                // ====== 路径2：直接注册（但同样携带了 visitorId） ======
                // 插入前唯一性校验：不能有相同 user_id / visitor_id（不排除任何行）
                hasUniqueUser(userDTO.getUserId(), visitorId, userDTO.getPhone(), null);

                user = new User();
                BeanUtils.copyProperties(userDTO, user);

                user.setVisitorId(visitorId);
                user.setRole(RoleEnum.USER.name());
                user.setStatus(1);
                user.setCreateTime(LocalDateTime.now());
                user.setUpdateTime(LocalDateTime.now());
                user.setLastIp(ip);
                user.setLastSeen(LocalDateTime.now());


                userMapper.insert(user);
            }
        } catch (DuplicateKeyException ex) {
            // 并发下可能仍被唯一索引拦截，这里给前端更友好的提示
            throw new BusinessException("注册冲突：userId 或 visitorId 已存在，请更换后重试");
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        return userVO;
    }

    private void hasUniqueUser(String userId, String visitorId, String phone, Long excludeId) {
        checkUnique("visitor_id", visitorId, excludeId, "该游客用户已存在，请更换浏览器环境再试");
        checkUnique("user_id", userId, excludeId, "用户ID已存在");
        checkUnique("phone", phone, excludeId, "手机号已存在");
    }


    private void checkUnique(String column, String value, Long excludeId, String message) {
        if (ObjectUtils.isEmpty(value)) {
            return;
        }
        long count = userMapper.selectCount(new QueryWrapper<User>()
                .eq(column, value)
                .ne(excludeId != null, "id", excludeId));
        if (count > 0) {
            throw new BusinessException(message);
        }
    }
}
