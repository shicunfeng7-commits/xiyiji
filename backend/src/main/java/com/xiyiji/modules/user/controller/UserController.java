package com.xiyiji.modules.user.controller;

import com.xiyiji.common.result.R;
import com.xiyiji.modules.order.entity.Order;
import com.xiyiji.modules.order.service.OrderService;
import com.xiyiji.modules.system.entity.ServiceTimeConfig;
import com.xiyiji.modules.system.service.ServiceTimeConfigService;
import com.xiyiji.modules.admin.entity.Admin;
import com.xiyiji.modules.admin.service.AdminService;
import com.xiyiji.modules.user.entity.User;
import com.xiyiji.modules.user.service.UserService;
import jakarta.annotation.Resource;
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

    /**
     * 用户登录/注册（按手机号）
     */
    @PostMapping("/login")
    public R<User> login(@RequestParam String phone) {
        return R.success(userService.loginOrRegister(phone));
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
        Order created = orderService.createOrder(order);
        return R.success(created);
    }

    /**
     * 获取用户订单列表
     */
    @GetMapping("/orders/{userId}")
    public R<List<Order>> getOrders(@PathVariable Long userId) {
        return R.success(orderService.getUserOrders(userId));
    }

    /**
     * 获取管理员微信二维码
     */
    @GetMapping("/admin-qrcode")
    public R<String> getAdminQrcode() {
        Admin admin = adminService.getById(1L);
        return R.success(admin != null ? admin.getWechatQrcode() : null);
    }

    /**
     * 订单详情
     */
    @GetMapping("/order/{id}")
    public R<Order> getOrder(@PathVariable Long id) {
        return R.success(orderService.getById(id));
    }
}