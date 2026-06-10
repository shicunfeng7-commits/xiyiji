package com.xiyiji.modules.employee.controller;

import com.xiyiji.common.result.R;
import com.xiyiji.modules.employee.entity.Employee;
import com.xiyiji.modules.employee.service.EmployeeService;
import com.xiyiji.modules.order.entity.Order;
import com.xiyiji.modules.order.service.OrderService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    @Resource
    private HttpServletRequest request;

    @Resource
    private EmployeeService employeeService;

    @Resource
    private OrderService orderService;

    /**
     * 获取当前员工信息（从 token 中解析 userId）
     */
    @GetMapping("/info")
    public R<Employee> getInfo() {
        Long userId = (Long) request.getAttribute("userId");
        Employee employee = employeeService.getByUserId(userId);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("该用户不是员工");
    }

    /**
     * 获取可抢订单列表（查询 PAID 状态且无 employee_id 的订单）
     */
    @GetMapping("/orders/available")
    public R<List<Order>> getAvailableOrders() {
        return R.success(orderService.getAvailableOrders());
    }

    /**
     * 抢单（从 token 获取 userId → 查 employee → 抢单）
     */
    @PostMapping("/order/grab/{orderId}")
    public R<Void> grabOrder(@PathVariable Long orderId) {
        Long userId = (Long) request.getAttribute("userId");
        Employee employee = employeeService.getByUserId(userId);
        if (employee == null) {
            return R.error("该用户不是员工");
        }
        boolean success = orderService.grabOrder(orderId, employee.getId());
        return success ? R.success() : R.error("抢单失败，该订单已被抢走");
    }

    /**
     * 我的订单（查询 employee_id = 当前员工）
     */
    @GetMapping("/orders/my-list")
    public R<List<Order>> getMyOrders() {
        Long userId = (Long) request.getAttribute("userId");
        Employee employee = employeeService.getByUserId(userId);
        if (employee == null) {
            return R.error("该用户不是员工");
        }
        return R.success(orderService.getEmployeeOrders(employee.getId()));
    }

    /**
     * 开始服务（员工只能操作自己的订单）
     */
    @PostMapping("/order/start/{orderId}")
    public R<Void> startOrder(@PathVariable Long orderId) {
        Long userId = (Long) request.getAttribute("userId");
        Employee employee = employeeService.getByUserId(userId);
        if (employee == null) {
            return R.error("该用户不是员工");
        }
        boolean success = orderService.startOrder(orderId, employee.getId());
        return success ? R.success() : R.error("开始服务失败，订单状态异常");
    }

    /**
     * 完成服务（员工只能操作自己的订单）
     */
    @PostMapping("/order/complete/{orderId}")
    public R<Void> completeOrder(@PathVariable Long orderId) {
        Long userId = (Long) request.getAttribute("userId");
        Employee employee = employeeService.getByUserId(userId);
        if (employee == null) {
            return R.error("该用户不是员工");
        }
        boolean success = orderService.completeOrder(orderId, employee.getId());
        return success ? R.success() : R.error("完成服务失败，订单状态异常");
    }
}