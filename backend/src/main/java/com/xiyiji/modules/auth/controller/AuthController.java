package com.xiyiji.modules.auth.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiyiji.common.result.R;
import com.xiyiji.common.util.JwtTokenUtil;
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
     * 管理员登录（使用统一 user 表，role=0 即为管理员）
     */
    @PostMapping("/admin/login")
    public R<Map<String, Object>> adminLogin(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getPassword, password)
                .eq(User::getRole, 0);
        User user = userMapper.selectOne(wrapper);

        if (user != null) {
            String token = JwtTokenUtil.generateToken(user.getId(), user.getPhone(), "admin");

            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("name", user.getNickname());
            userInfo.put("role", "admin");

            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            result.put("userInfo", userInfo);
            return R.success(result);
        }
        return R.error(401, "账号或密码错误");
    }
}