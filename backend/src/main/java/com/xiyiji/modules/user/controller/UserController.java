package com.xiyiji.modules.user.controller;

import com.xiyiji.common.constant.OrderStatus;
import com.xiyiji.common.result.R;
import com.xiyiji.modules.employee.entity.EmployeeApplication;
import com.xiyiji.modules.employee.service.EmployeeService;
import com.xiyiji.modules.order.entity.Order;
import com.xiyiji.modules.order.service.OrderService;
import com.xiyiji.modules.order.entity.OrderStatusLog;
import com.xiyiji.modules.order.service.OrderStatusLogService;
import com.xiyiji.modules.system.entity.ServiceTimeConfig;
import com.xiyiji.modules.system.service.ServiceTimeConfigService;
import com.xiyiji.modules.user.entity.User;
import com.xiyiji.modules.user.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    private EmployeeService employeeService;

    @Resource
    private OrderStatusLogService orderStatusLogService;

    @Resource
    private com.xiyiji.modules.review.service.OrderReviewService orderReviewService;

    @Resource
    private com.xiyiji.common.util.JwtTokenUtil jwtTokenUtil;

    @Resource
    private HttpServletRequest request;

    /**
     * /?
     */
    @PostMapping("/phone-login")
    public R<java.util.Map<String, Object>> login(@RequestBody java.util.Map<String, String> body) {
        String phone = body.get("phone");
        User user = userService.loginOrRegister(phone);
        String roleStr = user.getRole() != null && user.getRole() == 1 ? "employee" : "user";
        String token = jwtTokenUtil.generateToken(user.getId(), user.getPhone(), roleStr);
        
        java.util.Map<String, Object> userInfo = new java.util.HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("phone", user.getPhone());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("role", roleStr);
        
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("token", token);
        result.put("user", userInfo);
        return R.success(result);
    }

    /**
     * ?     */
    @GetMapping("/time-slots")
    public R<List<ServiceTimeConfig>> getTimeSlots() {
        return R.success(timeConfigService.getEnabledConfigs());
    }

    /**
     * ?
     */
    @PostMapping("/order/create")
    public R<Order> createOrder(@RequestBody Order order) {
        try {
            Long userId = (Long) request.getAttribute("userId");
            order.setUserId(userId);
            com.xiyiji.modules.user.entity.User u = userService.getById(userId);
            if (u != null) order.setUserName(u.getPhone());
            Order created = orderService.createOrder(order);
            
            // ?
            java.util.Map<String, Object> message = new java.util.HashMap<>();
            message.put("type", "NEW_ORDER");
            message.put("orderId", created.getId());
            message.put("orderNo", created.getOrderNo());
            message.put("buildingName", created.getBuildingName());
            message.put("roomNo", created.getRoomNo());
            message.put("serviceDate", created.getServiceDate() != null ? created.getServiceDate().toString() : "");
            message.put("startTime", created.getStartTime());
            message.put("endTime", created.getEndTime());
            message.put("amount", created.getAmount() != null ? created.getAmount().doubleValue() : 0);
            message.put("createTime", java.time.LocalDateTime.now().toString());
            
            com.xiyiji.common.websocket.EmployeeWebSocketServer.broadcastNewOrder(
                new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(message)
            );
            
            return R.success(created);
        } catch (Exception e) {
            e.printStackTrace();
            return R.error(e.getMessage());
        }
    }

    /**
     * ?     */
    @GetMapping("/order/list")
    public R<List<java.util.Map<String, Object>>> getOrders() {
        Long userId = (Long) request.getAttribute("userId");
        java.util.List<Order> orders = orderService.getUserOrders(userId);
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (Order o : orders) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", o.getId());
            map.put("orderNo", o.getOrderNo());
            map.put("buildingName", o.getBuildingName());
            map.put("roomNo", o.getRoomNo());
            map.put("contactPhone", o.getContactPhone());
            map.put("serviceDate", o.getServiceDate());
            map.put("startTime", o.getStartTime());
            map.put("endTime", o.getEndTime());
            map.put("status", o.getStatus());
            map.put("amount", o.getAmount());
            map.put("remark", o.getRemark());
            map.put("createTime", o.getCreateTime());
            map.put("payTime", o.getPayTime());
            map.put("completeTime", o.getCompleteTime());
            map.put("beforePhoto", o.getBeforePhoto());
            map.put("afterPhoto", o.getAfterPhoto());
            if (o.getEmployeeId() != null) {
                com.xiyiji.modules.employee.entity.Employee emp = employeeService.getById(o.getEmployeeId());
                map.put("employeeName", emp != null ? emp.getName() : "anonymous");
            } else {
                map.put("employeeName", null);
            }
            result.add(map);
        }
        return R.success(result);
    }

    /**
     * ?user ?
     */
    @GetMapping("/admin/qrcode")
    public R<String> getAdminQrcode() {
        return R.success(null);
    }

    /**
     * ?
     */
    @GetMapping("/order/detail/{id}")
    public R<Order> getOrder(@PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        return R.success(orderService.getOrderDetail(id, userId));
    }

    /**
     * ?
     */
    @PostMapping("/order/cancel/{id}")
    public R<Void> cancelOrder(@PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        orderService.cancelOrder(id, userId);
        return R.success(null);
    }

    /**
     * ?
     */
    @PostMapping("/apply-employee")
    public R<Void> applyEmployee(@RequestBody java.util.Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        String name = body.get("name");
        String phone = body.get("phone");
        String major = body.get("major");
        String grade = body.get("grade");
        userService.applyEmployee(userId, name, phone, major, grade);
        return R.success(null);
    }

    /**
     * ?     */
    @GetMapping("/apply-status")
    public R<EmployeeApplication> getApplyStatus() {
        Long userId = (Long) request.getAttribute("userId");
        return R.success(userService.getApplyStatus(userId));
    }

    /**
     * ?
     */
    @GetMapping("/profile")
    public R<Map<String, Object>> getProfile() {
        Long userId = (Long) request.getAttribute("userId");
        User user = userService.getById(userId);
        if (user == null) return R.error("not found");
        
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("id", user.getId());
        result.put("phone", user.getPhone());
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        result.put("buildingName", user.getBuildingName());
        result.put("roomNo", user.getRoomNo());
        result.put("role", user.getRole() != null && user.getRole() == 1 ? "employee" : "user");
        result.put("createTime", user.getCreateTime());
        result.put("updateTime", user.getUpdateTime());
        return R.success(result);
    }

    /**
     * ?
     */
    @PutMapping("/profile")
    public R<Void> updateProfile(@RequestBody java.util.Map<String, String> body) {
        Long userId = (Long) request.getAttribute("userId");
        com.xiyiji.modules.user.entity.User user = userService.getById(userId);
        if (user == null) return R.error("not found");
        if (body.containsKey("nickname")) user.setNickname(body.get("nickname"));
        if (body.containsKey("avatar")) user.setAvatar(body.get("avatar"));
        if (body.containsKey("phone")) user.setPhone(body.get("phone"));
        if (body.containsKey("buildingName")) user.setBuildingName(body.get("buildingName"));
        if (body.containsKey("roomNo")) user.setRoomNo(body.get("roomNo"));
        userService.updateById(user);
        return R.success();
    }

    /**
     * 
     */
    @GetMapping("/notifications")
    public R<java.util.Map<String, Object>> getNotifications() {
        Long userId = (Long) request.getAttribute("userId");
        com.xiyiji.modules.employee.entity.EmployeeApplication app = userService.getApplyStatus(userId);
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        if (app != null) {
            if (app.getStatus() == 1) {
                result.put("type", "approved");
                result.put("message", "");
            } else if (app.getStatus() == 2) {
                result.put("type", "rejected");
                result.put("message", "" + (app.getRemark() != null ? app.getRemark() : ""));
                // ?            } else {
                result.put("type", "pending");
                result.put("type", "none");
                result.put("message", "");
            }
        } else {
            result.put("type", "none");
            result.put("message", "");
        }
        return R.success(result);
    }

    /**
     * ?     */
    @GetMapping("/order/logs/{orderId}")
    public R<List<OrderStatusLog>> getOrderLogs(@PathVariable Long orderId) {
        return R.success(orderStatusLogService.getOrderLogs(orderId));
    }

    /**
     * ?     */
    @GetMapping("/reviews/featured")
    public R<List<Map<String, Object>>> getFeaturedReviews() {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.xiyiji.modules.review.entity.OrderReview> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        wrapper.eq(com.xiyiji.modules.review.entity.OrderReview::getIsFeatured, 1)
               .orderByDesc(com.xiyiji.modules.review.entity.OrderReview::getCreateTime)
               .last("LIMIT 6");

        List<com.xiyiji.modules.review.entity.OrderReview> reviews = orderReviewService.list(wrapper);
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        for (com.xiyiji.modules.review.entity.OrderReview r : reviews) {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", r.getId());
            item.put("score", r.getScore());
            item.put("content", r.getContent());
            item.put("createTime", r.getCreateTime());
            if (r.getUserId() != null) {
                User user = userService.getById(r.getUserId());
                item.put("nickname", user != null ? user.getNickname() : "");
                item.put("avatar", user != null ? user.getAvatar() : null);
            }
            if (r.getOrderId() != null) {
                Order order = orderService.getById(r.getOrderId());
                if (order != null) {
                    item.put("beforePhoto", order.getBeforePhoto());
                    item.put("afterPhoto", order.getAfterPhoto());
                    item.put("featuredPhotos", order.getFeaturedPhotos());
                }
            }
            result.add(item);
        }
        return R.success(result);
    }

    /**
     * ?     */
    @GetMapping("/photos/featured")
    public R<List<Map<String, Object>>> getFeaturedPhotos() {
        List<Order> orders = orderService.list(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                .eq(Order::getIsPhotoFeatured, 1)
                .eq(Order::getStatus, OrderStatus.COMPLETED)
                .orderByAsc(Order::getShowOrder)
        );
        
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (Order o : orders) {
            Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", o.getId());
            item.put("beforePhoto", o.getBeforePhoto());
            item.put("afterPhoto", o.getAfterPhoto());
            item.put("featuredPhotos", o.getFeaturedPhotos());
            item.put("buildingName", o.getBuildingName());
            item.put("showOrder", o.getShowOrder());

            if (o.getUserId() != null) {
                User user = userService.getById(o.getUserId());
                if (user != null) {
                    item.put("avatar", user.getAvatar());
                    String nickname = user.getNickname();
                    if (nickname != null && nickname.length() >= 2) {
                        item.put("nickname", nickname.substring(0, 1) + "***" + nickname.substring(nickname.length() - 1));
                    } else {
                        item.put("nickname", nickname != null ? nickname : "");
                    }
                }
            }

            com.xiyiji.modules.review.entity.OrderReview review = orderReviewService.getReviewByOrderId(o.getId());
            if (review != null) {
                item.put("score", review.getScore());
                item.put("content", review.getContent());
                item.put("createTime", review.getCreateTime());
            }

            result.add(item);
        }
        return R.success(result);
    }
}

