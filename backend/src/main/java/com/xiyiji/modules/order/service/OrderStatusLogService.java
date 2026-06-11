package com.xiyiji.modules.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiyiji.modules.order.entity.OrderStatusLog;

import java.util.List;

public interface OrderStatusLogService extends IService<OrderStatusLog> {
    void log(Long orderId, Integer fromStatus, Integer toStatus, Integer operatorType, Long operatorId);
    List<OrderStatusLog> getOrderLogs(Long orderId);
}
