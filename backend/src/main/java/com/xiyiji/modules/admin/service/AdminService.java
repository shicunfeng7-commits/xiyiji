package com.xiyiji.modules.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiyiji.modules.admin.entity.Admin;

public interface AdminService extends IService<Admin> {
    Admin login(String username, String password);
}