package com.xiyiji.modules.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiyiji.common.constant.OrderStatus;
import com.xiyiji.modules.employee.entity.Employee;
import com.xiyiji.modules.employee.service.EmployeeService;
import com.xiyiji.modules.order.entity.Order;
import com.xiyiji.modules.order.mapper.OrderMapper;
import com.xiyiji.modules.order.service.OrderService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final AtomicInteger SEQ = new AtomicInteger(0);

    @Resource
    private EmployeeService employeeService;

    @Resource
    private com.xiyiji.modules.order.service.OrderStatusLogService orderStatusLogService;

    @Override
    public String generateOrderNo() {
        LocalDate now = LocalDate.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = SEQ.incrementAndGet() % 10000;
        return "XS" + datePart + String.format("%04d", seq);
    }

    @Override
    @Transactional
    public Order createOrder(Order order) {
        // 检查同一楼栋同一天是否有时间重叠的订单
        if (hasTimeConflict(order)) {
            throw new RuntimeException("该时间段已有订单，请选择其他时间");
        }
        
        order.setOrderNo(generateOrderNo());
        order.setStatus(OrderStatus.UNPAID);
        order.setAmount(java.math.BigDecimal.valueOf(29.90));
        save(order);
        return order;
    }
    
    /**
     * 检查是否存在时间冲突的订单
     */
    private boolean hasTimeConflict(Order newOrder) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getBuildingCategory, newOrder.getBuildingCategory())
               .eq(Order::getBuildingName, newOrder.getBuildingName())
               .eq(Order::getServiceDate, newOrder.getServiceDate())
               .ne(Order::getStatus, OrderStatus.CANCELLED);
        
        List<Order> existingOrders = list(wrapper);
        
        String newStart = newOrder.getStartTime();
        String newEnd = newOrder.getEndTime();
        
        for (Order order : existingOrders) {
            String existingStart = order.getStartTime();
            String existingEnd = order.getEndTime();
            
            // 时间重叠判断：新订单开始时间 < 现有订单结束时间 && 新订单结束时间 > 现有订单开始时间
            if (newStart.compareTo(existingEnd) < 0 && newEnd.compareTo(existingStart) > 0) {
                return true;
            }
        }
        
        return false;
    }

    @Override
    @Transactional
    public boolean confirmPay(Long orderId, Long adminId) {
        Order order = getById(orderId);
        if (order == null || order.getStatus() != OrderStatus.UNPAID) {
            return false;
        }
        int fromStatus = order.getStatus();
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getId, orderId)
               .eq(Order::getStatus, OrderStatus.UNPAID)
               .set(Order::getStatus, OrderStatus.PAID)
               .set(Order::getPayTime, LocalDateTime.now());
        boolean result = update(wrapper);
        if (result) {
            orderStatusLogService.log(orderId, fromStatus, OrderStatus.PAID, 1, adminId);
        }
        return result;
    }

    @Override
    @Transactional
    public boolean revertPay(Long orderId) {
        Order order = getById(orderId);
        if (order == null || order.getStatus() != OrderStatus.PAID) {
            return false;
        }
        int fromStatus = order.getStatus();
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getId, orderId)
               .eq(Order::getStatus, OrderStatus.PAID)
               .set(Order::getStatus, OrderStatus.UNPAID)
               .set(Order::getPayTime, null);
        boolean result = update(wrapper);
        if (result) {
            orderStatusLogService.log(orderId, fromStatus, OrderStatus.UNPAID, 1, null);
        }
        return result;
    }

    @Override
    @Transactional
    public boolean grabOrder(Long orderId, Long employeeId) {
        Order order = getById(orderId);
        if (order == null || order.getStatus() != OrderStatus.PAID || order.getEmployeeId() != null) {
            return false;
        }
        int fromStatus = order.getStatus();
        // 乐观锁：只有 PAID 状态且无 employee_id 的才能抢
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getId, orderId)
               .eq(Order::getStatus, OrderStatus.PAID)
               .isNull(Order::getEmployeeId)
               .set(Order::getEmployeeId, employeeId)
               .set(Order::getStatus, OrderStatus.IN_PROGRESS);
        boolean updated = update(wrapper);
        if (updated) {
            orderStatusLogService.log(orderId, fromStatus, OrderStatus.IN_PROGRESS, 2, employeeId);
            // 更新员工状态为服务中
            employeeService.update(
                    new LambdaUpdateWrapper<Employee>()
                            .eq(Employee::getId, employeeId)
                            .set(Employee::getStatus, 1)
            );
        }
        return updated;
    }

    @Override
    @Transactional
    public boolean startOrder(Long orderId, Long employeeId) {
        Order order = getById(orderId);
        if (order == null || !employeeId.equals(order.getEmployeeId()) || order.getStatus() != OrderStatus.PAID) {
            return false;
        }
        int fromStatus = order.getStatus();
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getId, orderId)
               .eq(Order::getEmployeeId, employeeId)
               .eq(Order::getStatus, OrderStatus.PAID)
               .set(Order::getStatus, OrderStatus.IN_PROGRESS);
        boolean result = update(wrapper);
        if (result) {
            orderStatusLogService.log(orderId, fromStatus, OrderStatus.IN_PROGRESS, 2, employeeId);
        }
        return result;
    }

    @Override
    @Transactional
    public boolean completeOrder(Long orderId, Long employeeId, String beforePhotosJson, String afterPhotosJson) {
        Order order = getById(orderId);
        if (order == null || !employeeId.equals(order.getEmployeeId()) || order.getStatus() != OrderStatus.IN_PROGRESS) {
            return false;
        }
        if (beforePhotosJson == null || afterPhotosJson == null || beforePhotosJson.isEmpty() || afterPhotosJson.isEmpty()) {
            return false;
        }
        int fromStatus = order.getStatus();
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getId, orderId)
               .eq(Order::getEmployeeId, employeeId)
               .eq(Order::getStatus, OrderStatus.IN_PROGRESS)
               .set(Order::getStatus, OrderStatus.COMPLETED)
               .set(Order::getCompleteTime, LocalDateTime.now())
               .set(Order::getBeforePhoto, beforePhotosJson)
               .set(Order::getAfterPhoto, afterPhotosJson);
        boolean updated = update(wrapper);
        if (updated) {
            orderStatusLogService.log(orderId, fromStatus, OrderStatus.COMPLETED, 2, employeeId);
            employeeService.update(
                    new LambdaUpdateWrapper<Employee>()
                            .eq(Employee::getId, employeeId)
                            .set(Employee::getStatus, 0)
            );
        }
        return updated;
    }

    @Override
    public List<Order> getAvailableOrders() {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getStatus, OrderStatus.PAID)
               .isNull(Order::getEmployeeId)
               .orderByAsc(Order::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<Order> getUserOrders(Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId)
               .orderByDesc(Order::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<Order> getEmployeeOrders(Long employeeId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getEmployeeId, employeeId)
               .orderByDesc(Order::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<Order> getAllOrders(Integer status, String sort, String order, String keyword) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Order::getOrderNo, keyword).or().like(Order::getUserName, keyword));
        }
        
        boolean isDesc = "desc".equalsIgnoreCase(order);
        
        if ("serviceTime".equals(sort)) {
            if (isDesc) {
                wrapper.orderByDesc(Order::getServiceDate).orderByDesc(Order::getStartTime);
            } else {
                wrapper.orderByAsc(Order::getServiceDate).orderByAsc(Order::getStartTime);
            }
        } else if ("status".equals(sort)) {
            wrapper.orderByAsc(Order::getStatus);
        } else {
            if (isDesc) {
                wrapper.orderByDesc(Order::getCreateTime);
            } else {
                wrapper.orderByAsc(Order::getCreateTime);
            }
        }
        
        return list(wrapper);
    }

    @Override
    public Order getOrderDetail(Long id, Long userId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getId, id)
               .eq(Order::getUserId, userId);
        return getOne(wrapper);
    }

    @Override
    @Transactional
    public boolean cancelOrder(Long id, Long userId) {
        Order order = getById(id);
        if (order == null || !userId.equals(order.getUserId()) || order.getStatus() != OrderStatus.UNPAID) {
            return false;
        }
        int fromStatus = order.getStatus();
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getId, id)
               .eq(Order::getUserId, userId)
               .eq(Order::getStatus, OrderStatus.UNPAID)
               .set(Order::getStatus, OrderStatus.CANCELLED);
        boolean result = update(wrapper);
        if (result) {
            orderStatusLogService.log(id, fromStatus, OrderStatus.CANCELLED, 0, userId);
        }
        return result;
    }
}