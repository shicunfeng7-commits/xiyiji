package com.xiyiji.modules.admin.controller;

import com.xiyiji.common.result.R;
import com.xiyiji.modules.admin.entity.Admin;
import com.xiyiji.modules.admin.service.AdminService;
import com.xiyiji.modules.employee.entity.Employee;
import com.xiyiji.modules.employee.entity.EmployeeApplication;
import com.xiyiji.modules.employee.service.EmployeeService;
import com.xiyiji.modules.order.entity.Order;
import com.xiyiji.modules.order.service.OrderService;
import com.xiyiji.modules.system.entity.ServiceTimeConfig;
import com.xiyiji.modules.system.service.ServiceTimeConfigService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Resource
    private AdminService adminService;

    @Resource
    private OrderService orderService;

    @Resource
    private EmployeeService employeeService;

    @Resource
    private ServiceTimeConfigService timeConfigService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public R<Admin> login(@RequestParam String username, @RequestParam String password) {
        Admin admin = adminService.login(username, password);
        if (admin != null) {
            return R.success(admin);
        }
        return R.error(401, "账号或密码错误");
    }

    /**
     * 获取所有订单（可按状态筛选）
     */
    @GetMapping("/orders")
    public R<List<Order>> getOrders(@RequestParam(required = false) Integer status) {
        return R.success(orderService.getAllOrders(status));
    }

    /**
     * 确认已支付
     */
    @PostMapping("/order/confirm-pay/{orderId}")
    public R<Void> confirmPay(@PathVariable Long orderId, @RequestParam Long adminId) {
        boolean success = orderService.confirmPay(orderId, adminId);
        return success ? R.success() : R.error("确认失败，订单状态异常");
    }

    // ====== 员工管理 ======

    /**
     * 获取所有员工
     */
    @GetMapping("/employees")
    public R<List<Employee>> getEmployees() {
        return R.success(employeeService.list());
    }

    /**
     * 添加员工
     */
    @PostMapping("/employee/add")
    public R<Void> addEmployee(@RequestBody Employee employee) {
        employee.setStatus(0);
        employeeService.save(employee);
        return R.success();
    }

    /**
     * 删除员工
     */
    @DeleteMapping("/employee/{id}")
    public R<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.removeById(id);
        return R.success();
    }

    // ====== 服务时间段配置 ======

    /**
     * 获取所有时间段配置
     */
    @GetMapping("/time-configs")
    public R<List<ServiceTimeConfig>> getTimeConfigs() {
        return R.success(timeConfigService.list());
    }

    /**
     * 更新时间段配置
     */
    @PutMapping("/time-config/update")
    public R<Void> updateTimeConfig(@RequestBody ServiceTimeConfig config) {
        timeConfigService.updateConfig(config);
        return R.success();
    }

    // ====== 订单回退 ======

    /**
     * 订单回退支付（PAID -> UNPAID）
     */
    @PostMapping("/order/revert-pay/{id}")
    public R<Void> revertPay(@PathVariable Long id, @RequestParam Long adminId) {
        try {
            boolean success = adminService.revertPay(id, adminId);
            return success ? R.success() : R.error("回退失败，订单状态异常");
        } catch (RuntimeException e) {
            return R.error(e.getMessage());
        }
    }

    // ====== 员工申请审核 ======

    /**
     * 获取员工申请列表（可按状态筛选）
     */
    @GetMapping("/employee/applications")
    public R<List<EmployeeApplication>> getApplications(@RequestParam(required = false) Integer status) {
        return R.success(adminService.getApplications(status));
    }

    /**
     * 通过员工申请
     */
    @PostMapping("/employee/approve/{id}")
    public R<Void> approveApplication(@PathVariable Long id, @RequestParam Long adminId) {
        boolean success = adminService.approveApplication(id, adminId);
        return success ? R.success() : R.error("审核失败，申请状态异常");
    }

    /**
     * 拒绝员工申请
     */
    @PostMapping("/employee/reject/{id}")
    public R<Void> rejectApplication(@PathVariable Long id, @RequestParam Long adminId,
                                     @RequestParam String remark) {
        boolean success = adminService.rejectApplication(id, adminId, remark);
        return success ? R.success() : R.error("操作失败，申请状态异常");
    }
}