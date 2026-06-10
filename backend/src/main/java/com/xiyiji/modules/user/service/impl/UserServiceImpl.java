package com.xiyiji.modules.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiyiji.modules.employee.entity.EmployeeApplication;
import com.xiyiji.modules.employee.mapper.EmployeeApplicationMapper;
import com.xiyiji.modules.user.entity.User;
import com.xiyiji.modules.user.mapper.UserMapper;
import com.xiyiji.modules.user.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Resource
    private EmployeeApplicationMapper employeeApplicationMapper;

    @Override
    public User loginOrRegister(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = getOne(wrapper);
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setNickname("用户" + phone.substring(phone.length() - 4));
            save(user);
        }
        return user;
    }

    @Override
    public void applyEmployee(Long userId, String name) {
        EmployeeApplication application = new EmployeeApplication();
        application.setUserId(userId);
        application.setName(name);
        application.setStatus(0);
        application.setCreateTime(LocalDateTime.now());
        employeeApplicationMapper.insert(application);
    }

    @Override
    public EmployeeApplication getApplyStatus(Long userId) {
        LambdaQueryWrapper<EmployeeApplication> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmployeeApplication::getUserId, userId)
               .orderByDesc(EmployeeApplication::getCreateTime)
               .last("LIMIT 1");
        return employeeApplicationMapper.selectOne(wrapper);
    }
}