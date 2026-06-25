package com.xiyiji.common.interceptor;

import com.xiyiji.common.util.JwtTokenUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"æªç»å½ætokenæ æ\"}");
            return false;
        }

        String token = authHeader.substring(7);
        if (!jwtTokenUtil.validateToken(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"msg\":\"tokenå·²è¿æææ æ\"}");
            return false;
        }

        Long userId = jwtTokenUtil.parseUserId(token);
        String phone = jwtTokenUtil.parsePhone(token);
        String role = jwtTokenUtil.parseRole(token);
        request.setAttribute("userId", userId);
        request.setAttribute("phone", phone);
        request.setAttribute("role", role);

        String path = request.getRequestURI();
        if (path.startsWith("/api/admin")) {
            if (!"admin".equals(role)) {
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":403,\"msg\":\"æ ç®¡çåæé\"}");
                return false;
            }
        }

        return true;
    }
}
