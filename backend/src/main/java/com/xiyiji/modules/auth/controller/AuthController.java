package com.xiyiji.modules.auth.controller;

import com.xiyiji.common.dto.AdminLoginDTO;
import com.xiyiji.common.dto.UserLoginDTO;
import com.xiyiji.common.result.R;
import com.xiyiji.common.util.JwtTokenUtil;
import com.xiyiji.common.vo.LoginVO;
import com.xiyiji.common.vo.UserInfoVO;
import com.xiyiji.modules.auth.service.AuthService;
import com.xiyiji.modules.auth.service.SmsService;
import com.xiyiji.modules.user.entity.User;
import com.xiyiji.modules.user.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "è®¤è¯æ¥å£", description = "ç»å½ãè·åç¨æ·ä¿¡æ¯")
public class AuthController {

    @Resource
    private AuthService authService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private SmsService smsService;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Operation(summary = "åééªè¯ç ")
    @PostMapping("/sms/send")
    public R<Void> sendSmsCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || phone.isBlank()) {
            return R.error("ææºå·ä¸è½ä¸ºç©º");
        }
        try {
            smsService.sendCode(phone);
            return R.success();
        } catch (RuntimeException e) {
            return R.error(e.getMessage());
        }
    }

    @Operation(summary = "ç¨æ·/åå·¥ææºå·ç»å½")
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody UserLoginDTO dto) {
        return R.success(authService.login(dto.getPhone(), dto.getCode()));
    }

    @Operation(summary = "è·åå½åç»å½ç¨æ·ä¿¡æ¯")
    @GetMapping("/user-info")
    public R<UserInfoVO> userInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userMapper.selectById(userId);
        if (user == null) {
            return R.error("ç¨æ·ä¸å­å¨");
        }
        UserInfoVO vo = new UserInfoVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        vo.setBuildingName(user.getBuildingName());
        vo.setRoomNo(user.getRoomNo());
        vo.setRole(user.getRole() == 0 ? "admin" : user.getRole() == 1 ? "employee" : "user");
        return R.success(vo);
    }

    @Operation(summary = "ç®¡çåè´¦å·å¯ç ç»å½")
    @PostMapping("/admin/login")
    public R<LoginVO> adminLogin(@Valid @RequestBody AdminLoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername())
                .eq(User::getRole, 0);
        User user = userMapper.selectOne(wrapper);

        if (user != null && passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            String token = jwtTokenUtil.generateToken(user.getId(), user.getPhone(), "admin");

            UserInfoVO userInfo = new UserInfoVO();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setNickname(user.getNickname());
            userInfo.setRole("admin");

            return R.success(new LoginVO(token, userInfo));
        }
        return R.error(401, "è´¦å·æå¯ç éè¯¯");
    }
}
