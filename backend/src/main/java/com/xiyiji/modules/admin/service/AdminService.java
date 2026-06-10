package com.xiyiji.modules.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiyiji.modules.admin.entity.Admin;
import com.xiyiji.modules.employee.entity.EmployeeApplication;

import java.util.List;

public interface AdminService extends IService<Admin> {
    Admin login(String username, String password);
    boolean revertPay(Long orderId, Long adminId);
    List<EmployeeApplication> getApplications(Integer status);
    boolean approveApplication(Long applicationId, Long adminId);
    boolean rejectApplication(Long applicationId, Long adminId, String remark);
}