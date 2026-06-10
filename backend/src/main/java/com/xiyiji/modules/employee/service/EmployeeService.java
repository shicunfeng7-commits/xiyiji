package com.xiyiji.modules.employee.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiyiji.modules.employee.entity.Employee;

public interface EmployeeService extends IService<Employee> {
    Employee login(String username, String password);
}