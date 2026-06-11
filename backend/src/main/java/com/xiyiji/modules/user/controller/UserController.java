package com.xiyiji.modules.user.controller;

import com.xiyiji.common.result.R;
import com.xiyiji.modules.employee.entity.EmployeeApplication;
import com.xiyiji.modules.order.entity.Order;
import com.xiyiji.modules.order.service.OrderService;
import com.xiyiji.modules.system.entity.ServiceTimeConfig;
import com.xiyiji.modules.system.service.ServiceTimeConfigService;
import com.xiyiji.modules.admin.entity.Admin;
import com.xiyiji.modules.admin.service.AdminService;
import com.xiyiji.modules.user.entity.User;
import com.xiyiji.modules.user.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private OrderService orderService;

    @Resource
    private ServiceTimeConfigService timeConfigService;

    @Resource
    private UserService userService;

    @Resource
    private AdminService adminService;

    @Resource
    private HttpServletRequest request;

    /**
     * 用户登录/注册（按手机号）
     */
    @PostMapping("/login")
    public R<java.util.Map<String, Object>> login(@RequestBody java.util.Map<String, String> body) {
        String phone = body.get("phone");
        User user = userService.loginOrRegister(phone);
        String token = com.xiyiji.common.util.JwtTokenUtil.generateToken(user.getId(), user.getPhone());
        
        java.util.Map<String, Object> userInfo = new java.util.HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("phone", user.getPhone());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("role", user.getRole());
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("token", token);
        result.put("user", userInfo);
        return R.success(result);
    }

    /**
     * 获取可用服务时间段
     */
    @GetMapping("/time-slots")
    public R<List<ServiceTimeConfig>> getTimeSlots() {
        return R.success(timeConfigService.getEnabledConfigs());
    }

    /**
     * 创建订单
     */
    @PostMapping("/order/create")
    public R<Order> createOrder(@RequestBody Order order) {
        Long userId = (Long) request.getAttribute("userId");
        order.setUserId(userId);
        Order created = orderService.createOrder(order);
        return R.success(created);
    }

    /**
     * 我的订单列表
     */
    @GetMapping("/order/list")
    public R<List<Order>> getOrders() {
        Long userId = (Long) request.getAttribute("userId");
        return R.success(orderService.getUserOrders(userId));
    }

    /**
     * 获取管理员微信二维码
     */
    @GetMapping("/admin/qrcode")
    public R<String> getAdminQrcode() {
        Admin admin = adminService.getById(1L);
        return R.success(admin != null ? admin.getWechatQrcode() : null);
    }

    /**
     * 订单详情
     */
    @GetMapping("/order/detail/{id}")
    public R<Order> getOrder(@PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return R.success(orderService.getOrderDetail(id, userId));
    }

    /**
     * 取消订单
     */
    @PostMapping("/order/cancel/{id}")
    public R<Void> cancelOrder(@PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.cancelOrder(id, userId);
        return R.success(null);
    }

    /**
     * 申请成为员工
     */
    @PostMapping("/apply-employee")
    public R<Void> applyEmployee(@RequestBody java.util.Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        String name = body.get("name");
        String phone = body.get("phone");
        String major = body.get("major");
        userService.applyEmployee(userId, name, phone, major);
        return R.success(null);
    }

    /**
     * 查看申请状态
     */
    @GetMapping("/apply-status")
    public R<EmployeeApplication> getApplyStatus() {
        Long userId = (Long) request.getAttribute("userId");
        return R.success(userService.getApplyStatus(userId));
    }
}