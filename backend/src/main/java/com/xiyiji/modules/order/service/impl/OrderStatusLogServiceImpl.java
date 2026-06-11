package com.xiyiji.modules.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiyiji.modules.order.entity.OrderStatusLog;
import com.xiyiji.modules.order.mapper.OrderStatusLogMapper;
import com.xiyiji.modules.order.service.OrderStatusLogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderStatusLogServiceImpl extends ServiceImpl<OrderStatusLogMapper, OrderStatusLog> implements OrderStatusLogService {

    @Override
    public void log(Long orderId, Integer fromStatus, Integer toStatus, Integer operatorType, Long operatorId) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderId(orderId);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperatorType(operatorType);
        log.setOperatorId(operatorId);
        log.setCreateTime(LocalDateTime.now());
        save(log);
    }

    @Override
    public List<OrderStatusLog> getOrderLogs(Long orderId) {
        LambdaQueryWrapper<OrderStatusLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderStatusLog::getOrderId, orderId)
               .orderByAsc(OrderStatusLog::getCreateTime);
        return list(wrapper);
    }
}
