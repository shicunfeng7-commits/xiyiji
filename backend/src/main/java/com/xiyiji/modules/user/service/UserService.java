package com.xiyiji.modules.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiyiji.modules.employee.entity.EmployeeApplication;
import com.xiyiji.modules.user.entity.User;

public interface UserService extends IService<User> {
    User loginOrRegister(String phone);
    void applyEmployee(Long userId, String name, String phone, String major, String grade);
    EmployeeApplication getApplyStatus(Long userId);
}