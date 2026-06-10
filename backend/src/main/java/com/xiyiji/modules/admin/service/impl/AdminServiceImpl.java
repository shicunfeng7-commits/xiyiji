package com.xiyiji.modules.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiyiji.common.constant.OrderStatus;
import com.xiyiji.modules.admin.entity.Admin;
import com.xiyiji.modules.admin.mapper.AdminMapper;
import com.xiyiji.modules.admin.service.AdminService;
import com.xiyiji.modules.employee.entity.Employee;
import com.xiyiji.modules.employee.entity.EmployeeApplication;
import com.xiyiji.modules.employee.mapper.EmployeeApplicationMapper;
import com.xiyiji.modules.employee.mapper.EmployeeMapper;
import com.xiyiji.modules.order.entity.Order;
import com.xiyiji.modules.order.entity.OrderStatusLog;
import com.xiyiji.modules.order.mapper.OrderMapper;
import com.xiyiji.modules.order.mapper.OrderStatusLogMapper;
import com.xiyiji.modules.order.service.OrderService;
import com.xiyiji.modules.user.entity.User;
import com.xiyiji.modules.user.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Resource
    private OrderService orderService;

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private OrderStatusLogMapper orderStatusLogMapper;

    @Resource
    private EmployeeApplicationMapper employeeApplicationMapper;

    @Resource
    private EmployeeMapper employeeMapper;

    @Resource
    private UserMapper userMapper;

    @Override
    public Admin login(String username, String password) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, username)
               .eq(Admin::getPassword, password);
        return getOne(wrapper);
    }

    @Override
    @Transactional
    public boolean revertPay(Long orderId, Long adminId) {
        // 查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return false;
        }
        if (order.getStatus() != OrderStatus.PAID) {
            return false;
        }
        if (order.getEmployeeId() != null) {
            throw new RuntimeException("已抢单的订单不可回退");
        }
        // 调用 OrderService 修改状态
        boolean success = orderService.revertPay(orderId);
        if (success) {
            // 记录操作日志
            OrderStatusLog log = new OrderStatusLog();
            log.setOrderId(orderId);
            log.setFromStatus(OrderStatus.PAID);
            log.setToStatus(OrderStatus.UNPAID);
            log.setOperatorType(1); // 1-管理员
            log.setOperatorId(adminId);
            log.setCreateTime(LocalDateTime.now());
            orderStatusLogMapper.insert(log);
        }
        return success;
    }

    @Override
    public List<EmployeeApplication> getApplications(Integer status) {
        LambdaQueryWrapper<EmployeeApplication> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(EmployeeApplication::getStatus, status);
        } else {
            wrapper.eq(EmployeeApplication::getStatus, 0); // 默认查 PENDING
        }
        wrapper.orderByDesc(EmployeeApplication::getCreateTime);
        return employeeApplicationMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public boolean approveApplication(Long applicationId, Long adminId) {
        // 1. 查询申请
        EmployeeApplication application = employeeApplicationMapper.selectById(applicationId);
        if (application == null || application.getStatus() != 0) {
            return false;
        }

        // 2. 创建 Employee 记录
        Employee employee = new Employee();
        employee.setUserId(application.getUserId());
        employee.setName(application.getName());
        employee.setPhone(application.getPhone());
        employee.setIsActive(1);
        employee.setStatus(0);
        employee.setCreateTime(LocalDateTime.now());
        employeeMapper.insert(employee);

        // 3. 更新 User 表的 role = 1
        User user = userMapper.selectById(application.getUserId());
        if (user != null) {
            user.setRole(1);
            userMapper.updateById(user);
        }

        // 4. 更新 EmployeeApplication 的 status = 1（已通过）
        application.setStatus(1);
        application.setHandlerId(adminId);
        application.setHandleTime(LocalDateTime.now());
        employeeApplicationMapper.updateById(application);

        return true;
    }

    @Override
    @Transactional
    public boolean rejectApplication(Long applicationId, Long adminId, String remark) {
        EmployeeApplication application = employeeApplicationMapper.selectById(applicationId);
        if (application == null || application.getStatus() != 0) {
            return false;
        }

        application.setStatus(2); // 已拒绝
        application.setRemark(remark);
        application.setHandlerId(adminId);
        application.setHandleTime(LocalDateTime.now());
        employeeApplicationMapper.updateById(application);
        return true;
    }
}