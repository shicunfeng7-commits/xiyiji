package com.xiyiji.modules.auth.service;

import com.xiyiji.common.util.JwtTokenUtil;
import com.xiyiji.common.vo.LoginVO;
import com.xiyiji.common.vo.UserInfoVO;
import com.xiyiji.modules.employee.entity.Employee;
import com.xiyiji.modules.employee.mapper.EmployeeMapper;
import com.xiyiji.modules.user.entity.User;
import com.xiyiji.modules.user.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private EmployeeMapper employeeMapper;

    @Resource
    private SmsService smsService;

    public LoginVO login(String phone, String code) {
        User user = userMapper.selectByPhone(phone);

        if (user != null) {
            return buildLoginVO(user);
        }

        if (code == null || code.isBlank()) {
            throw new RuntimeException("该手机号未注册，请输入验证码完成注册");
        }

        if (!smsService.verifyCode(phone, code)) {
            throw new RuntimeException("验证码错误或已过期");
        }

        user = new User();
        user.setPhone(phone);
        user.setNickname("用户" + phone.substring(phone.length() - 4));
        user.setRole(2);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);

        return buildLoginVO(user);
    }

    private LoginVO buildLoginVO(User user) {
        String roleStr;
        if (user.getRole() == 0) {
            roleStr = "admin";
        } else if (user.getRole() == 1) {
            roleStr = "employee";
        } else {
            roleStr = "user";
        }
        String token = JwtTokenUtil.generateToken(user.getId(), user.getPhone(), roleStr);

        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setId(user.getId());
        userInfo.setPhone(user.getPhone());
        userInfo.setNickname(user.getNickname());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setBuildingName(user.getBuildingName());
        userInfo.setRoomNo(user.getRoomNo());
        userInfo.setRole(roleStr);

        if (user.getRole() != null && user.getRole() == 1) {
            Employee employee = employeeMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Employee>()
                            .eq(Employee::getUserId, user.getId())
            );
            if (employee != null) {
                userInfo.setEmployeeId(employee.getId());
            }
        }

        return new LoginVO(token, userInfo);
    }
}