package com.xiyiji.modules.admin.controller;

import com.xiyiji.common.result.R;
import com.xiyiji.modules.admin.entity.Admin;
import com.xiyiji.modules.admin.service.AdminService;
import com.xiyiji.modules.employee.entity.Employee;
import com.xiyiji.modules.employee.entity.EmployeeApplication;
import com.xiyiji.modules.employee.service.EmployeeService;
import com.xiyiji.modules.order.entity.Order;
import com.xiyiji.modules.order.entity.OrderStatusLog;
import com.xiyiji.modules.order.service.OrderService;
import com.xiyiji.modules.order.service.OrderStatusLogService;
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

    @Resource
    private OrderStatusLogService orderStatusLogService;

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public R<java.util.Map<String, Object>> login(@RequestBody java.util.Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        Admin admin = adminService.login(username, password);
        if (admin != null) {
            String token = com.xiyiji.common.util.JwtTokenUtil.generateToken(admin.getId(), admin.getUsername());
            
            java.util.Map<String, Object> userInfo = new java.util.HashMap<>();
            userInfo.put("id", admin.getId());
            userInfo.put("username", admin.getUsername());
            userInfo.put("name", admin.getName());
            userInfo.put("role", 2);
            
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("token", token);
            result.put("userInfo", userInfo);
            return R.success(result);
        }
        return R.error(401, "账号或密码错误");
    }

    /**
     * 获取所有订单（可按状态筛选和排序）
     */
    @GetMapping("/orders")
    public R<List<Order>> getOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false, defaultValue = "createTime") String sort,
            @RequestParam(required = false, defaultValue = "desc") String order) {
        return R.success(orderService.getAllOrders(status, sort, order));
    }

    /**
     * 确认已支付
     */
    @PostMapping("/order/confirm-pay/{orderId}")
    public R<Void> confirmPay(@PathVariable Long orderId, @RequestParam Long adminId) {
        boolean success = orderService.confirmPay(orderId, adminId);
        return success ? R.success() : R.error("确认失败，订单状态异常");
    }

    /**
     * 获取订单操作日志
     */
    @GetMapping("/order/logs/{orderId}")
    public R<List<OrderStatusLog>> getOrderLogs(@PathVariable Long orderId) {
        return R.success(orderStatusLogService.getOrderLogs(orderId));
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

    /**
     * 获取数据看板统计数据
     */
    @GetMapping("/dashboard")
    public R<java.util.Map<String, Object>> getDashboard(@RequestParam(required = false, defaultValue = "week") String range) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        
        // 订单统计
        long totalOrders = orderService.count();
        result.put("totalOrders", totalOrders);
        result.put("orderTrend", calculateTrend(120, 100));
        
        // 营收统计
        java.math.BigDecimal totalRevenue = orderService.list().stream()
                .filter(o -> o.getStatus() == 3)
                .map(com.xiyiji.modules.order.entity.Order::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        result.put("totalRevenue", totalRevenue.doubleValue());
        result.put("revenueTrend", calculateTrend(5800, 5000));
        
        // 员工统计
        long activeEmployees = employeeService.count();
        result.put("activeEmployees", activeEmployees);
        result.put("employeeTrend", calculateTrend(15, 12));
        
        // 评价统计
        double avgRating = 4.8;
        result.put("avgRating", avgRating);
        result.put("ratingTrend", 2);
        
        // 订单状态分布
        java.util.Map<String, Object> statusDistribution = new java.util.HashMap<>();
        statusDistribution.put("unpaid", orderService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.xiyiji.modules.order.entity.Order>()
                .eq(com.xiyiji.modules.order.entity.Order::getStatus, 0)));
        statusDistribution.put("paid", orderService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.xiyiji.modules.order.entity.Order>()
                .eq(com.xiyiji.modules.order.entity.Order::getStatus, 1)));
        statusDistribution.put("inProgress", orderService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.xiyiji.modules.order.entity.Order>()
                .eq(com.xiyiji.modules.order.entity.Order::getStatus, 2)));
        statusDistribution.put("completed", orderService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.xiyiji.modules.order.entity.Order>()
                .eq(com.xiyiji.modules.order.entity.Order::getStatus, 3)));
        result.put("statusDistribution", statusDistribution);
        
        // 营收趋势（模拟数据）
        java.util.List<java.util.Map<String, Object>> revenueTrend = new java.util.ArrayList<>();
        int[] amounts = {850, 1200, 980, 1500, 1100, 1800, 1350};
        for (int i = 0; i < 7; i++) {
            java.util.Map<String, Object> day = new java.util.HashMap<>();
            day.put("amount", amounts[i]);
            revenueTrend.add(day);
        }
        result.put("revenueTrend", revenueTrend);
        
        // 楼栋订单排行（模拟数据）
        java.util.List<java.util.Map<String, Object>> buildingRanking = new java.util.ArrayList<>();
        String[] buildings = {"食宿楼1栋", "食宿楼2栋", "学生宿舍1栋", "学生宿舍2栋", "教师公寓A栋"};
        int[] buildingCounts = {45, 38, 32, 28, 25};
        for (int i = 0; i < buildings.length; i++) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("name", buildings[i]);
            item.put("count", buildingCounts[i]);
            buildingRanking.add(item);
        }
        result.put("buildingRanking", buildingRanking);
        
        // 员工服务排行（模拟数据）
        java.util.List<java.util.Map<String, Object>> employeeRanking = new java.util.ArrayList<>();
        String[] employees = {"张师傅", "李师傅", "王师傅"};
        int[] employeeCounts = {28, 24, 20};
        for (int i = 0; i < employees.length; i++) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", i + 1);
            item.put("name", employees[i]);
            item.put("count", employeeCounts[i]);
            item.put("revenue", employeeCounts[i] * 29);
            employeeRanking.add(item);
        }
        result.put("employeeRanking", employeeRanking);
        
        return R.success(result);
    }
    
    private int calculateTrend(long current, long previous) {
        if (previous == 0) return 0;
        return (int) ((current - previous) * 100 / previous);
    }
}