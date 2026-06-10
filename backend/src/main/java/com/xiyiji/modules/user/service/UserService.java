package com.xiyiji.modules.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiyiji.modules.user.entity.User;

public interface UserService extends IService<User> {
    User loginOrRegister(String phone);
}