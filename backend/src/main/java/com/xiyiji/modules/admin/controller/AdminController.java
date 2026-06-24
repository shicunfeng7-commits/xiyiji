package com.xiyiji.modules.admin.controller;

import com.xiyiji.common.dto.AdminLoginDTO;
import com.xiyiji.common.result.R;
import com.xiyiji.common.vo.LoginVO;
import com.xiyiji.common.vo.UserInfoVO;
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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "管理端接口", description = "管理员登录、订单管理、员工管理、数据看板")
public class AdminController {

    @Resource
    private AdminService adminService;

    @Resource
    private OrderService orderService;

    @Resource
    private EmployeeService employeeService;

    @Resource
    private com.xiyiji.modules.user.mapper.UserMapper userMapper;

    @Resource
    private com.xiyiji.modules.employee.mapper.EmployeeMapper employeeMapper;

    @Resource
    private ServiceTimeConfigService timeConfigService;

    @Resource
    private OrderStatusLogService orderStatusLogService;

    @Resource
    private com.xiyiji.modules.review.service.OrderReviewService orderReviewService;

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody AdminLoginDTO dto) {
        Admin admin = adminService.login(dto.getUsername(), dto.getPassword());
        if (admin != null) {
            String token = com.xiyiji.common.util.JwtTokenUtil.generateToken(admin.getId(), admin.getUsername(), "admin");

            UserInfoVO userInfo = new UserInfoVO();
            userInfo.setId(admin.getId());
            userInfo.setUsername(admin.getUsername());
            userInfo.setNickname(admin.getName());
            userInfo.setRole("admin");

            return R.success(new LoginVO(token, userInfo));
        }
        return R.error(401, "账号或密码错误");
    }

    @Operation(summary = "获取所有订单（支持状态筛选、排序、关键词搜索、精选筛选）")
    @GetMapping("/orders")
    public R<List<java.util.Map<String, Object>>> getOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer featured,
            @RequestParam(required = false, defaultValue = "createTime") String sort,
            @RequestParam(required = false, defaultValue = "desc") String order,
            @RequestParam(required = false) String keyword) {
        List<Order> orders = orderService.getAllOrders(status, featured, sort, order, keyword);
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (Order o : orders) {
            java.util.Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", o.getId());
            map.put("orderNo", o.getOrderNo());
            map.put("userId", o.getUserId());
            map.put("buildingName", o.getBuildingName());
            map.put("buildingCategory", o.getBuildingCategory());
            map.put("roomNo", o.getRoomNo());
            map.put("contactPhone", o.getContactPhone());
            map.put("serviceDate", o.getServiceDate());
            map.put("startTime", o.getStartTime());
            map.put("endTime", o.getEndTime());
            map.put("status", o.getStatus());
            map.put("employeeId", o.getEmployeeId());
            map.put("amount", o.getAmount());
            map.put("remark", o.getRemark());
            map.put("beforePhoto", o.getBeforePhoto());
            map.put("afterPhoto", o.getAfterPhoto());
            map.put("isPhotoFeatured", o.getIsPhotoFeatured());
            map.put("showOrder", o.getShowOrder());
            map.put("featuredPhotos", o.getFeaturedPhotos());
            map.put("createTime", o.getCreateTime());
            map.put("payTime", o.getPayTime());
            map.put("completeTime", o.getCompleteTime());
            // 管理员可看到用户手机号
            if (o.getUserId() != null) {
                com.xiyiji.modules.user.entity.User user = userMapper.selectById(o.getUserId());
                map.put("userPhone", user != null ? user.getPhone() : null);
            } else {
                map.put("userPhone", null);
            }
            // 员工名
            if (o.getEmployeeId() != null) {
                com.xiyiji.modules.employee.entity.Employee emp = employeeMapper.selectById(o.getEmployeeId());
                map.put("employeeName", emp != null ? emp.getName() : null);
            } else {
                map.put("employeeName", null);
            }
            result.add(map);
        }
        return R.success(result);
    }

    @Operation(summary = "确认订单已支付")
    @PostMapping("/order/confirm-pay/{orderId}")
    public R<Void> confirmPay(@PathVariable Long orderId, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
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
        employee.setStatus(com.xiyiji.common.constant.EmployeeStatus.FREE);
        if (employee.getIsActive() == null) {
            employee.setIsActive(com.xiyiji.common.constant.EmployeeStatus.ENABLED);
        }
        employeeService.save(employee);
        return R.success();
    }

    /**
     * 启用/停用员工
     */
    @PostMapping("/employee/toggle/{id}")
    public R<Void> toggleEmployee(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        if (employee == null) {
            return R.error("员工不存在");
        }
        employee.setIsActive(employee.getIsActive() == com.xiyiji.common.constant.EmployeeStatus.ENABLED
                ? com.xiyiji.common.constant.EmployeeStatus.DISABLED
                : com.xiyiji.common.constant.EmployeeStatus.ENABLED);
        employeeService.updateById(employee);
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

    /**
     * 删除订单（管理员）
     */
    @DeleteMapping("/order/{id}")
    public R<Void> deleteOrder(@PathVariable Long id) {
        boolean removed = orderService.removeById(id);
        return removed ? R.success() : R.error("订单不存在");
    }

    // ====== 服务时间段配置 ======

    /**
     * 获取所有时间段配置（前端格式：{key, label, enabled, startTime, endTime}）
     */
    @GetMapping("/time-config/list")
    public R<List<java.util.Map<String, Object>>> getTimeConfigList() {
        List<ServiceTimeConfig> configs = timeConfigService.list();
        List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (ServiceTimeConfig c : configs) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("key", c.getPeriod().toLowerCase());
            item.put("label", c.getPeriodName());
            item.put("enabled", c.getEnabled());
            item.put("startTime", String.format("%02d:00", c.getStartHour()));
            item.put("endTime", String.format("%02d:00", c.getEndHour()));
            result.add(item);
        }
        return R.success(result);
    }

    /**
     * 批量更新时间段配置
     */
    @PutMapping("/time-config/update")
    public R<Void> updateTimeConfigs(@RequestBody List<java.util.Map<String, Object>> configs) {
        for (java.util.Map<String, Object> item : configs) {
            String key = ((String) item.get("key")).toUpperCase();
            ServiceTimeConfig entity = timeConfigService.lambdaQuery()
                    .eq(ServiceTimeConfig::getPeriod, key).one();
            if (entity != null) {
                entity.setEnabled((Boolean) item.get("enabled"));
                String startTime = (String) item.get("startTime");
                String endTime = (String) item.get("endTime");
                entity.setStartHour(Integer.parseInt(startTime.split(":")[0]));
                entity.setEndHour(Integer.parseInt(endTime.split(":")[0]));
                timeConfigService.updateConfig(entity);
            }
        }
        return R.success();
    }

    // ====== 订单回退 ======

    /**
     * 订单回退支付（PAID -> UNPAID）
     */
    @PostMapping("/order/revert-pay/{id}")
    public R<Void> revertPay(@PathVariable Long id, HttpServletRequest request) {
        try {
            Long adminId = (Long) request.getAttribute("userId");
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
    public R<Void> approveApplication(@PathVariable Long id, HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        boolean success = adminService.approveApplication(id, adminId);
        return success ? R.success() : R.error("审核失败，申请状态异常");
    }

    /**
     * 拒绝员工申请
     */
    @PostMapping("/employee/reject/{id}")
    public R<Void> rejectApplication(@PathVariable Long id, @RequestParam String remark,
                                     HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("userId");
        boolean success = adminService.rejectApplication(id, adminId, remark);
        return success ? R.success() : R.error("操作失败，申请状态异常");
    }

    @Operation(summary = "获取数据看板统计（订单、营收、员工排行等）")
    @GetMapping("/dashboard")
    public R<java.util.Map<String, Object>> getDashboard(@RequestParam(required = false, defaultValue = "week") String range) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        
        // 订单统计
        long totalOrders = orderService.count();
        result.put("totalOrders", totalOrders);
        result.put("orderTrend", 0);
        
        // 营收统计
        java.math.BigDecimal totalRevenue = orderService.list().stream()
                .filter(o -> o.getStatus() == com.xiyiji.common.constant.OrderStatus.COMPLETED)
                .map(com.xiyiji.modules.order.entity.Order::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);
        result.put("totalRevenue", totalRevenue.doubleValue());
        result.put("revenueTrend", 0);
        
        // 员工统计
        long activeEmployees = employeeService.count();
        result.put("activeEmployees", activeEmployees);
        result.put("employeeTrend", 0);
        
        // 评价统计
        result.put("avgRating", 0);
        result.put("ratingTrend", 0);
        
        // 订单状态分布
        java.util.Map<String, Object> statusDistribution = new java.util.HashMap<>();
        statusDistribution.put("unpaid", orderService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.xiyiji.modules.order.entity.Order>()
                .eq(com.xiyiji.modules.order.entity.Order::getStatus, com.xiyiji.common.constant.OrderStatus.UNPAID)));
        statusDistribution.put("paid", orderService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.xiyiji.modules.order.entity.Order>()
                .eq(com.xiyiji.modules.order.entity.Order::getStatus, com.xiyiji.common.constant.OrderStatus.PAID)));
        statusDistribution.put("pendingService", orderService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.xiyiji.modules.order.entity.Order>()
                .eq(com.xiyiji.modules.order.entity.Order::getStatus, com.xiyiji.common.constant.OrderStatus.PENDING_SERVICE)));
        statusDistribution.put("inProgress", orderService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.xiyiji.modules.order.entity.Order>()
                .eq(com.xiyiji.modules.order.entity.Order::getStatus, com.xiyiji.common.constant.OrderStatus.IN_PROGRESS)));
        statusDistribution.put("completed", orderService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.xiyiji.modules.order.entity.Order>()
                .eq(com.xiyiji.modules.order.entity.Order::getStatus, com.xiyiji.common.constant.OrderStatus.COMPLETED)));
        result.put("statusDistribution", statusDistribution);
        
        // 营收趋势
        java.util.List<java.util.Map<String, Object>> revenueTrend = new java.util.ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.util.List<com.xiyiji.modules.order.entity.Order> completedOrders = orderService.list(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.xiyiji.modules.order.entity.Order>()
                .eq(com.xiyiji.modules.order.entity.Order::getStatus, com.xiyiji.common.constant.OrderStatus.COMPLETED)
                .isNotNull(com.xiyiji.modules.order.entity.Order::getCompleteTime));
        String[] weekDays = {"周一","周二","周三","周四","周五","周六","周日"};

        if ("h1".equals(range) || "h2".equals(range)) {
            int startMonth = "h1".equals(range) ? 1 : 7;
            for (int m = startMonth; m < startMonth + 6; m++) {
                java.math.BigDecimal monthSum = java.math.BigDecimal.ZERO;
                for (com.xiyiji.modules.order.entity.Order o : completedOrders) {
                    if (o.getCompleteTime() != null) {
                        java.time.LocalDate d = o.getCompleteTime().toLocalDate();
                        if (d.getMonthValue() == m) {
                            monthSum = monthSum.add(o.getAmount() != null ? o.getAmount() : java.math.BigDecimal.ZERO);
                        }
                    }
                }
                java.util.Map<String, Object> item = new java.util.HashMap<>();
                item.put("label", m + "月"); item.put("amount", monthSum.doubleValue()); revenueTrend.add(item);
            }
        } else if ("month".equals(range)) {
            int daysInMonth = today.lengthOfMonth();
            for (int d = 1; d <= daysInMonth; d++) {
                java.time.LocalDate date = today.withDayOfMonth(d);
                java.math.BigDecimal daySum = java.math.BigDecimal.ZERO;
                for (com.xiyiji.modules.order.entity.Order o : completedOrders) {
                    if (o.getCompleteTime() != null && o.getCompleteTime().toLocalDate().equals(date)) {
                        daySum = daySum.add(o.getAmount() != null ? o.getAmount() : java.math.BigDecimal.ZERO);
                    }
                }
                java.util.Map<String, Object> item = new java.util.HashMap<>();
                item.put("label", d + "日"); item.put("amount", daySum.doubleValue()); revenueTrend.add(item);
            }
        } else {
            java.time.LocalDate monday = today.with(java.time.DayOfWeek.MONDAY);
            for (java.time.LocalDate d = monday; !d.isAfter(today); d = d.plusDays(1)) {
                int idx = d.getDayOfWeek().getValue() - 1;
                java.math.BigDecimal daySum = java.math.BigDecimal.ZERO;
                for (com.xiyiji.modules.order.entity.Order o : completedOrders) {
                    if (o.getCompleteTime() != null && o.getCompleteTime().toLocalDate().equals(d)) {
                        daySum = daySum.add(o.getAmount() != null ? o.getAmount() : java.math.BigDecimal.ZERO);
                    }
                }
                java.util.Map<String, Object> item = new java.util.HashMap<>();
                item.put("label", weekDays[idx]); item.put("amount", daySum.doubleValue()); revenueTrend.add(item);
            }
        }
        result.put("revenueTrend", revenueTrend);
        
        // 楼栋订单排行（从数据库统计）
        java.util.List<java.util.Map<String, Object>> buildingRanking = new java.util.ArrayList<>();
        java.util.Map<String, Long> buildingCountMap = new java.util.HashMap<>();
        orderService.list().forEach(order -> {
            String building = order.getBuildingName();
            if (building != null && building.contains("·")) {
                building = building.split("·")[0].trim();
            }
            buildingCountMap.merge(building, 1L, Long::sum);
        });
        buildingCountMap.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .forEach(entry -> {
                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("count", entry.getValue());
                    buildingRanking.add(item);
                });
        result.put("buildingRanking", buildingRanking);
        
        // 员工服务排行（从数据库统计）
        java.util.List<java.util.Map<String, Object>> employeeRanking = new java.util.ArrayList<>();
        java.util.Map<Long, java.util.Map<String, Object>> employeeStats = new java.util.HashMap<>();
        
        java.util.Map<Long, String> employeeNameMap = new java.util.HashMap<>();
        employeeService.list().forEach(emp -> {
            employeeNameMap.put(emp.getId(), emp.getName());
        });
        
        orderService.list(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.xiyiji.modules.order.entity.Order>()
                .eq(com.xiyiji.modules.order.entity.Order::getStatus, com.xiyiji.common.constant.OrderStatus.COMPLETED))
                .forEach(order -> {
                    Long empId = order.getEmployeeId();
                    if (empId != null) {
                        employeeStats.computeIfAbsent(empId, id -> {
                            java.util.Map<String, Object> stats = new java.util.HashMap<>();
                            stats.put("id", id);
                            stats.put("name", employeeNameMap.getOrDefault(id, "员工" + id));
                            stats.put("count", 0L);
                            stats.put("revenue", 0.0);
                            return stats;
                        });
                        java.util.Map<String, Object> stats = employeeStats.get(empId);
                        stats.put("count", (Long) stats.get("count") + 1);
                        stats.put("revenue", (Double) stats.get("revenue") + order.getAmount().doubleValue());
                    }
                });
        
        employeeStats.values().stream()
                .sorted((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")))
                .limit(5)
                .forEach(employeeRanking::add);
        result.put("employeeRanking", employeeRanking);
        
        return R.success(result);
    }

    // ====== 精选评价管理 ======

    /**
     * 获取所有评价列表（可按精选状态筛选）
     */
    @Operation(summary = "获取评价列表")
    @GetMapping("/reviews")
    public R<List<java.util.Map<String, Object>>> getReviews(
            @RequestParam(required = false) Integer featured) {
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.xiyiji.modules.review.entity.OrderReview> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        if (featured != null) {
            wrapper.eq(com.xiyiji.modules.review.entity.OrderReview::getIsFeatured, featured);
        }
        wrapper.orderByDesc(com.xiyiji.modules.review.entity.OrderReview::getCreateTime);
        
        List<com.xiyiji.modules.review.entity.OrderReview> reviewList = orderReviewService.list(wrapper);
        
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (com.xiyiji.modules.review.entity.OrderReview r : reviewList) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", r.getId());
            item.put("orderId", r.getOrderId());
            item.put("userId", r.getUserId());
            item.put("score", r.getScore());
            item.put("content", r.getContent());
            item.put("isFeatured", r.getIsFeatured());
            item.put("createTime", r.getCreateTime());
            if (r.getUserId() != null) {
                com.xiyiji.modules.user.entity.User user = userMapper.selectById(r.getUserId());
                item.put("nickname", user != null ? user.getNickname() : "匿名用户");
                item.put("phone", user != null ? user.getPhone() : "");
            }
            result.add(item);
        }
        return R.success(result);
    }

    @Operation(summary = "设置精选评价")
    @PostMapping("/review/featured/{id}")
    public R<Void> toggleFeatured(@PathVariable Long id, @RequestParam boolean featured) {
        com.xiyiji.modules.review.entity.OrderReview review = orderReviewService.getById(id);
        if (review == null) {
            return R.error("评价不存在");
        }
        review.setIsFeatured(featured ? 1 : 0);
        orderReviewService.updateById(review);
        return R.success();
    }

    // ====== 精选照片管理 ======

    /**
     * 设置/取消精选照片
     */
    @Operation(summary = "设置精选照片")
    @PostMapping("/order/photo-featured/{id}")
    public R<Void> togglePhotoFeatured(@PathVariable Long id, @RequestParam boolean featured) {
        Order order = orderService.getById(id);
        if (order == null) {
            return R.error("订单不存在");
        }
        order.setIsPhotoFeatured(featured ? 1 : 0);
        if (featured && (order.getShowOrder() == null || order.getShowOrder() == 0)) {
            // 设置展示顺序为当前最大值+1
            Long count = orderService.count(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                    .eq(Order::getIsPhotoFeatured, 1)
            );
            order.setShowOrder((int)(count + 1));
        } else if (!featured) {
            order.setShowOrder(0);
        }
        orderService.updateById(order);
        return R.success();
    }

    /**
     * 设置展示顺序
     */
    @Operation(summary = "设置展示顺序")
    @PostMapping("/order/show-order/{id}")
    public R<Void> setShowOrder(@PathVariable Long id, @RequestParam Integer showOrder) {
        Order order = orderService.getById(id);
        if (order == null) {
            return R.error("订单不存在");
        }
        order.setShowOrder(showOrder);
        orderService.updateById(order);
        return R.success();
    }

    /**
     * 保存精选照片（管理员选择的照片列表）
     */
    @Operation(summary = "保存精选照片")
    @PostMapping("/order/featured-photos/{id}")
    public R<Void> saveFeaturedPhotos(@PathVariable Long id, @RequestBody java.util.Map<String, Object> body) {
        Order order = orderService.getById(id);
        if (order == null) {
            return R.error("订单不存在");
        }
        String photos = (String) body.get("photos");
        order.setFeaturedPhotos(photos);
        order.setIsPhotoFeatured(photos != null && !photos.isEmpty() ? 1 : 0);
        orderService.updateById(order);
        return R.success();
    }

    /**
     * 获取精选照片列表（首页展示用）
     */
    @Operation(summary = "获取精选照片")
    @GetMapping("/orders/photo-featured")
    public R<List<java.util.Map<String, Object>>> getPhotoFeaturedOrders() {
        List<Order> orders = orderService.list(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Order>()
                .eq(Order::getIsPhotoFeatured, 1)
                .eq(Order::getStatus, com.xiyiji.common.constant.OrderStatus.COMPLETED)
                .orderByAsc(Order::getShowOrder)
        );
        
        java.util.List<java.util.Map<String, Object>> result = new java.util.ArrayList<>();
        for (Order o : orders) {
            java.util.Map<String, Object> item = new java.util.HashMap<>();
            item.put("id", o.getId());
            item.put("beforePhoto", o.getBeforePhoto());
            item.put("afterPhoto", o.getAfterPhoto());
            item.put("featuredPhotos", o.getFeaturedPhotos());
            item.put("buildingName", o.getBuildingName());
            item.put("showOrder", o.getShowOrder());
            item.put("completeTime", o.getCompleteTime());
            result.add(item);
        }
        return R.success(result);
    }
}