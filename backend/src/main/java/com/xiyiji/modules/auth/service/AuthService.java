package com.xiyiji.modules.auth.service;

import com.xiyiji.common.util.JwtTokenUtil;
import com.xiyiji.modules.employee.entity.Employee;
import com.xiyiji.modules.employee.mapper.EmployeeMapper;
import com.xiyiji.modules.user.entity.User;
import com.xiyiji.modules.user.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private EmployeeMapper employeeMapper;

    public Map<String, Object> login(String phone) {
        User user = userMapper.selectByPhone(phone);
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setNickname("用户" + phone.substring(phone.length() - 4));
            user.setRole(0);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            userMapper.insert(user);
        }

        String token = JwtTokenUtil.generateToken(user.getId(), user.getPhone());

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("phone", user.getPhone());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("role", user.getRole());

        // 如果 role=1（员工），查询 employee 表获取员工信息
        if (user.getRole() == 1) {
            Employee employee = employeeMapper.selectOne(
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Employee>()
                            .eq(Employee::getUserId, user.getId())
            );
            if (employee != null) {
                userInfo.put("employeeId", employee.getId());
                userInfo.put("employeeName", employee.getName());
                userInfo.put("employeeStatus", employee.getStatus());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", userInfo);
        return result;
    }
}