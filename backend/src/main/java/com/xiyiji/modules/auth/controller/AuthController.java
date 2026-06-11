package com.xiyiji.modules.auth.controller;

import com.xiyiji.common.result.R;
import com.xiyiji.modules.admin.entity.Admin;
import com.xiyiji.modules.admin.service.AdminService;
import com.xiyiji.modules.auth.service.AuthService;
import com.xiyiji.modules.user.entity.User;
import com.xiyiji.modules.user.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private AdminService adminService;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        return R.success(authService.login(phone));
    }

    @GetMapping("/user-info")
    public R<User> userInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userMapper.selectById(userId);
        return R.success(user);
    }

    /**
     * 管理员登录
     */
    @PostMapping("/admin/login")
    public R<Map<String, Object>> adminLogin(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        Admin admin = adminService.login(username, password);
        if (admin != null) {
            String token = com.xiyiji.common.util.JwtTokenUtil.generateToken(admin.getId(), admin.getUsername());
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", admin.getId());
            userInfo.put("username", admin.getUsername());
            userInfo.put("name", admin.getName());
            userInfo.put("role", 2);
            
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userInfo", userInfo);
            return R.success(result);
        }
        return R.error(401, "账号或密码错误");
    }
}