package com.xiyiji.modules.employee.controller;

import com.xiyiji.common.result.R;
import com.xiyiji.modules.employee.entity.Employee;
import com.xiyiji.modules.employee.service.EmployeeService;
import com.xiyiji.modules.order.entity.Order;
import com.xiyiji.modules.order.service.OrderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    @Resource
    private EmployeeService employeeService;

    @Resource
    private OrderService orderService;

    /**
     * 员工登录
     */
    @PostMapping("/login")
    public R<Employee> login(@RequestParam String username, @RequestParam String password) {
        Employee employee = employeeService.login(username, password);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error(401, "账号或密码错误");
    }

    /**
     * 获取可抢订单列表
     */
    @GetMapping("/orders/available")
    public R<List<Order>> getAvailableOrders() {
        return R.success(orderService.getAvailableOrders());
    }

    /**
     * 抢单
     */
    @PostMapping("/order/grab/{orderId}")
    public R<Void> grabOrder(@PathVariable Long orderId, @RequestParam Long employeeId) {
        boolean success = orderService.grabOrder(orderId, employeeId);
        return success ? R.success() : R.error("抢单失败，该订单已被抢走");
    }

    /**
     * 获取员工订单列表
     */
    @GetMapping("/orders/{employeeId}")
    public R<List<Order>> getMyOrders(@PathVariable Long employeeId) {
        return R.success(orderService.getEmployeeOrders(employeeId));
    }

    /**
     * 完成服务
     */
    @PostMapping("/order/complete/{orderId}")
    public R<Void> completeOrder(@PathVariable Long orderId, @RequestParam Long employeeId) {
        boolean success = orderService.completeOrder(orderId, employeeId);
        return success ? R.success() : R.error("完成失败，订单状态异常");
    }
}