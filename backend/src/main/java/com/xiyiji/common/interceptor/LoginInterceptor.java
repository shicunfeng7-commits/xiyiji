package com.xiyiji.common.interceptor;

import com.xiyiji.common.util.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或token无效\"}");
            return false;
        }

        String token = authHeader.substring(7);
        if (!JwtTokenUtil.validateToken(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"token已过期或无效\"}");
            return false;
        }

        Long userId = JwtTokenUtil.parseUserId(token);
        String phone = JwtTokenUtil.parsePhone(token);
        request.setAttribute("userId", userId);
        request.setAttribute("phone", phone);
        return true;
    }
}