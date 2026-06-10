package com.xiyiji.modules.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiyiji.modules.user.entity.User;

public interface UserMapper extends BaseMapper<User> {
    User selectByPhone(String phone);
}