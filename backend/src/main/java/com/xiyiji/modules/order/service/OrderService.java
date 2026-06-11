package com.xiyiji.modules.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiyiji.modules.order.entity.Order;

import java.util.List;

public interface OrderService extends IService<Order> {
    String generateOrderNo();
    Order createOrder(Order order);
    boolean confirmPay(Long orderId, Long adminId);
    boolean revertPay(Long orderId);
    boolean grabOrder(Long orderId, Long employeeId);
    boolean startOrder(Long orderId, Long employeeId);
    boolean completeOrder(Long orderId, Long employeeId);
    List<Order> getAvailableOrders(); // 待抢单列表
    List<Order> getUserOrders(Long userId);
    List<Order> getEmployeeOrders(Long employeeId);
    List<Order> getAllOrders(Integer status, String sort, String order);
    Order getOrderDetail(Long id, Long userId);
    boolean cancelOrder(Long id, Long userId);
}