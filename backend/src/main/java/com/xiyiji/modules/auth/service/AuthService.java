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
            user.setRole(2); // 默认角色: 用户
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            userMapper.insert(user);
        }

        // role: 0-管理员, 1-员工, 2-用户
        String roleStr;
        if (user.getRole() == 0) {
            roleStr = "admin";
        } else if (user.getRole() == 1) {
            roleStr = "employee";
        } else {
            roleStr = "user";
        }
        String token = JwtTokenUtil.generateToken(user.getId(), user.getPhone(), roleStr);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("phone", user.getPhone());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("buildingName", user.getBuildingName());
        userInfo.put("roomNo", user.getRoomNo());
        userInfo.put("role", roleStr);

        // 如果 role=1（员工），查询 employee 表获取员工信息
        if (user.getRole() != null && user.getRole() == 1) {
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