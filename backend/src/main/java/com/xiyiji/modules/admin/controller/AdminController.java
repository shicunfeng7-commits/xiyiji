package com.xiyiji.modules.admin.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiyiji.common.constant.OrderStatus;
import com.xiyiji.common.dto.AdminLoginDTO;
import com.xiyiji.common.result.R;
import com.xiyiji.common.util.JwtTokenUtil;
import com.xiyiji.common.vo.LoginVO;
import com.xiyiji.common.vo.UserInfoVO;
import com.xiyiji.modules.employee.entity.Employee;
import com.xiyiji.modules.employee.entity.EmployeeApplication;
import com.xiyiji.modules.employee.mapper.EmployeeApplicationMapper;
import com.xiyiji.modules.employee.mapper.EmployeeMapper;
import com.xiyiji.modules.employee.service.EmployeeService;
import com.xiyiji.modules.order.entity.Order;
import com.xiyiji.modules.order.service.OrderService;
import com.xiyiji.modules.order.service.OrderStatusLogService;
import com.xiyiji.modules.system.entity.ServiceTimeConfig;
import com.xiyiji.modules.system.service.ServiceTimeConfigService;
import com.xiyiji.modules.user.entity.User;
import com.xiyiji.modules.user.mapper.UserMapper;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin")
public class AdminController {

    @Resource
    private OrderService orderService;

    @Resource
    private EmployeeService employeeService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private EmployeeMapper employeeMapper;

    @Resource
    private EmployeeApplicationMapper employeeApplicationMapper;

    @Resource
    private ServiceTimeConfigService timeConfigService;

    @Resource
    private OrderStatusLogService orderStatusLogService;

    @Resource
    private com.xiyiji.modules.review.service.OrderReviewService orderReviewService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private JwtTokenUtil jwtTokenUtil;

    @Resource
    private HttpServletRequest request;

    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody AdminLoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername())
                .eq(User::getRole, 0);
        User user = userMapper.selectOne(wrapper);
        if (user != null && passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            String token = jwtTokenUtil.generateToken(user.getId(), user.getPhone(), "admin");
            UserInfoVO userInfo = new UserInfoVO();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setNickname(user.getNickname());
            userInfo.setRole("admin");
            return R.success(new LoginVO(token, userInfo));
        }
        return R.error(401, "wrong username or password");
    }

    @GetMapping("/orders")
    public R<List<Map<String, Object>>> getOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer featured,
            @RequestParam(required = false, defaultValue = "createTime") String sort,
            @RequestParam(required = false, defaultValue = "desc") String order,
            @RequestParam(required = false) String keyword) {
        List<Order> orders = orderService.getAllOrders(status, featured, sort, order, keyword);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order o : orders) {
            Map<String, Object> map = new HashMap<>();
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
            if (o.getUserId() != null) {
                User user = userMapper.selectById(o.getUserId());
                map.put("userPhone", user != null ? user.getPhone() : null);
            } else {
                map.put("userPhone", null);
            }
            if (o.getEmployeeId() != null) {
                Employee emp = employeeMapper.selectById(o.getEmployeeId());
                map.put("employeeName", emp != null ? emp.getName() : null);
            } else {
                map.put("employeeName", null);
            }
            result.add(map);
        }
        return R.success(result);
    }

    @GetMapping("/order/{id}")
    public R<Map<String, Object>> getOrderDetail(@PathVariable Long id) {
        Order o = orderService.getById(id);
        if (o == null) return R.error(404, "order not found");
        Map<String, Object> map = new HashMap<>();
        map.put("id", o.getId());
        map.put("orderNo", o.getOrderNo());
        map.put("userId", o.getUserId());
        map.put("userName", o.getUserName());
        map.put("buildingCategory", o.getBuildingCategory());
        map.put("buildingName", o.getBuildingName());
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
        map.put("createTime", o.getCreateTime());
        map.put("payTime", o.getPayTime());
        map.put("completeTime", o.getCompleteTime());
        if (o.getUserId() != null) {
            User user = userMapper.selectById(o.getUserId());
            map.put("userPhone", user != null ? user.getPhone() : "");
        }
        if (o.getEmployeeId() != null) {
            Employee emp = employeeMapper.selectById(o.getEmployeeId());
            map.put("employeeName", emp != null ? emp.getName() : "");
        }
        return R.success(map);
    }

    @PostMapping("/order/confirm-pay/{id}")
    @Transactional
    public R<Void> confirmPay(@PathVariable Long id) {
        Long adminId = (Long) request.getAttribute("userId");
        boolean success = orderService.confirmPay(id, adminId);
        return success ? R.success() : R.error("confirm pay failed");
    }

    @PostMapping("/order/revert-pay/{id}")
    @Transactional
    public R<Void> revertPay(@PathVariable Long id) {
        Long adminId = (Long) request.getAttribute("userId");
        Order order = orderService.getById(id);
        if (order == null) return R.error("order not found");
        if (order.getStatus() != OrderStatus.PAID) return R.error("order not in PAID status");
        if (order.getEmployeeId() != null) return R.error("cannot revert, order already grabbed");
        boolean success = orderService.revertPay(id);
        if (success) {
            orderStatusLogService.log(id, OrderStatus.PAID, OrderStatus.UNPAID, 1, adminId);
        }
        return success ? R.success() : R.error("revert failed");
    }

    @DeleteMapping("/order/{id}")
    public R<Void> deleteOrder(@PathVariable Long id) {
        orderService.removeById(id);
        return R.success();
    }

    @GetMapping("/applications")
    public R<List<EmployeeApplication>> getApplications(
            @RequestParam(required = false) Integer status) {
        LambdaQueryWrapper<EmployeeApplication> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(EmployeeApplication::getStatus, status);
        } else {
            wrapper.eq(EmployeeApplication::getStatus, 0);
        }
        wrapper.orderByDesc(EmployeeApplication::getCreateTime);
        return R.success(employeeApplicationMapper.selectList(wrapper));
    }

    @PostMapping("/application/approve/{id}")
    @Transactional
    public R<Void> approveApplication(@PathVariable Long id) {
        Long adminId = (Long) request.getAttribute("userId");
        EmployeeApplication application = employeeApplicationMapper.selectById(id);
        if (application == null || application.getStatus() != 0) {
            return R.error("application not found or already processed");
        }
        Employee employee = new Employee();
        employee.setUserId(application.getUserId());
        employee.setName(application.getName());
        employee.setPhone(application.getPhone());
        employee.setIsActive(1);
        employee.setStatus(0);
        employee.setCreateTime(LocalDateTime.now());
        employeeMapper.insert(employee);
        User user = userMapper.selectById(application.getUserId());
        if (user != null) {
            user.setRole(1);
            userMapper.updateById(user);
        }
        application.setStatus(1);
        application.setHandlerId(adminId);
        application.setHandleTime(LocalDateTime.now());
        employeeApplicationMapper.updateById(application);
        return R.success();
    }

    @PostMapping("/application/reject/{id}")
    @Transactional
    public R<Void> rejectApplication(@PathVariable Long id, @RequestParam String remark) {
        Long adminId = (Long) request.getAttribute("userId");
        EmployeeApplication application = employeeApplicationMapper.selectById(id);
        if (application == null || application.getStatus() != 0) {
            return R.error("application not found or already processed");
        }
        application.setStatus(2);
        application.setRemark(remark);
        application.setHandlerId(adminId);
        application.setHandleTime(LocalDateTime.now());
        employeeApplicationMapper.updateById(application);
        return R.success();
    }

    @GetMapping("/employees")
    public R<List<Employee>> getEmployees() {
        return R.success(employeeService.list());
    }

    @PostMapping("/employee/add")
    public R<Void> addEmployee(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        String phone = body.get("phone");
        if (name == null || name.isBlank()) return R.error("name is required");
        if (phone == null || phone.isBlank()) return R.error("phone is required");

        User user = new User();
        user.setPhone(phone);
        user.setNickname(name);
        user.setRole(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);

        Employee employee = new Employee();
        employee.setUserId(user.getId());
        employee.setName(name);
        employee.setPhone(phone);
        employee.setIsActive(1);
        employee.setStatus(0);
        employee.setCreateTime(LocalDateTime.now());
        employeeMapper.insert(employee);

        return R.success();
    }

    @PostMapping("/employee/toggle/{id}")
    public R<Void> toggleEmployee(@PathVariable Long id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) return R.error("employee not found");
        employee.setIsActive(employee.getIsActive() == 1 ? 0 : 1);
        employeeMapper.updateById(employee);
        return R.success();
    }

    @GetMapping("/time-configs")
    public R<List<ServiceTimeConfig>> getTimeConfigs() {
        return R.success(timeConfigService.getEnabledConfigs());
    }

    @PostMapping("/time-configs")
    public R<Void> saveTimeConfigs(@RequestBody List<ServiceTimeConfig> configs) {
        timeConfigService.saveOrUpdateBatch(configs);
        return R.success();
    }

    @GetMapping("/dashboard")
    public R<Map<String, Object>> getDashboard() {
        Map<String, Object> result = new HashMap<>();
        long totalOrders = orderService.count();
        long completedOrders = orderService.count(
                new LambdaQueryWrapper<Order>().eq(Order::getStatus, OrderStatus.COMPLETED));
        long totalEmployees = employeeService.count();
        long totalUsers = userMapper.selectCount(null);
        result.put("totalOrders", totalOrders);
        result.put("completedOrders", completedOrders);
        result.put("totalEmployees", totalEmployees);
        result.put("totalUsers", totalUsers);

        List<Map<String, Object>> revenueTrend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(6 - i);
            LocalDateTime dayStart = day.atStartOfDay();
            LocalDateTime dayEnd = day.plusDays(1).atStartOfDay();
            Double daySum = orderService.getBaseMapper().selectObjs(
                    new LambdaQueryWrapper<Order>()
                            .eq(Order::getStatus, OrderStatus.COMPLETED)
                            .between(Order::getCompleteTime, dayStart, dayEnd)
                            .select(Order::getAmount)
            ).stream().filter(Objects::nonNull).map(o -> (BigDecimal) o)
                    .map(BigDecimal::doubleValue).reduce(0.0, Double::sum);
            Map<String, Object> item = new HashMap<>();
            item.put("label", sdf.format(java.sql.Date.valueOf(day)));
            item.put("amount", daySum);
            revenueTrend.add(item);
        }
        result.put("revenueTrend", revenueTrend);

        Map<String, Long> buildingCountMap = new HashMap<>();
        orderService.list().forEach(order -> {
            String building = order.getBuildingName();
            if (building != null) {
                buildingCountMap.merge(building, 1L, Long::sum);
            }
        });
        List<Map<String, Object>> buildingRanking = new ArrayList<>();
        buildingCountMap.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(5)
                .forEach(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", entry.getKey());
                    item.put("count", entry.getValue());
                    buildingRanking.add(item);
                });
        result.put("buildingRanking", buildingRanking);

        Map<Long, String> employeeNameMap = new HashMap<>();
        employeeService.list().forEach(emp -> employeeNameMap.put(emp.getId(), emp.getName()));
        Map<Long, Map<String, Object>> employeeStats = new HashMap<>();
        orderService.list(new LambdaQueryWrapper<Order>().eq(Order::getStatus, OrderStatus.COMPLETED))
                .forEach(order -> {
                    Long empId = order.getEmployeeId();
                    if (empId != null) {
                        employeeStats.computeIfAbsent(empId, id -> {
                            Map<String, Object> stats = new HashMap<>();
                            stats.put("id", id);
                            stats.put("name", employeeNameMap.getOrDefault(id, "emp" + id));
                            stats.put("count", 0L);
                            stats.put("revenue", 0.0);
                            return stats;
                        });
                        Map<String, Object> stats = employeeStats.get(empId);
                        stats.put("count", (Long) stats.get("count") + 1);
                        stats.put("revenue", (Double) stats.get("revenue") + order.getAmount().doubleValue());
                    }
                });
        List<Map<String, Object>> employeeRanking = new ArrayList<>();
        employeeStats.values().stream()
                .sorted((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")))
                .limit(5)
                .forEach(employeeRanking::add);
        result.put("employeeRanking", employeeRanking);
        return R.success(result);
    }

    @GetMapping("/reviews")
    public R<List<Map<String, Object>>> getReviews(@RequestParam(required = false) Integer featured) {
        LambdaQueryWrapper<com.xiyiji.modules.review.entity.OrderReview> wrapper = new LambdaQueryWrapper<>();
        if (featured != null) {
            wrapper.eq(com.xiyiji.modules.review.entity.OrderReview::getIsFeatured, featured);
        }
        wrapper.orderByDesc(com.xiyiji.modules.review.entity.OrderReview::getCreateTime);
        List<com.xiyiji.modules.review.entity.OrderReview> reviewList = orderReviewService.list(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (com.xiyiji.modules.review.entity.OrderReview r : reviewList) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("orderId", r.getOrderId());
            item.put("userId", r.getUserId());
            item.put("score", r.getScore());
            item.put("content", r.getContent());
            item.put("isFeatured", r.getIsFeatured());
            item.put("createTime", r.getCreateTime());
            if (r.getUserId() != null) {
                User user = userMapper.selectById(r.getUserId());
                item.put("nickname", user != null ? user.getNickname() : "anonymous");
                item.put("phone", user != null ? user.getPhone() : "");
            }
            result.add(item);
        }
        return R.success(result);
    }

    @PostMapping("/review/featured/{id}")
    public R<Void> toggleFeatured(@PathVariable Long id, @RequestParam boolean featured) {
        com.xiyiji.modules.review.entity.OrderReview review = orderReviewService.getById(id);
        if (review == null) return R.error("review not found");
        review.setIsFeatured(featured ? 1 : 0);
        orderReviewService.updateById(review);
        return R.success();
    }

    @PostMapping("/order/photo-featured/{id}")
    public R<Void> togglePhotoFeatured(@PathVariable Long id, @RequestParam boolean featured) {
        Order order = orderService.getById(id);
        if (order == null) return R.error("order not found");
        order.setIsPhotoFeatured(featured ? 1 : 0);
        if (featured && (order.getShowOrder() == null || order.getShowOrder() == 0)) {
            Long count = orderService.count(new LambdaQueryWrapper<Order>().eq(Order::getIsPhotoFeatured, 1));
            order.setShowOrder((int) (count + 1));
        } else if (!featured) {
            order.setShowOrder(0);
        }
        orderService.updateById(order);
        return R.success();
    }

    @PostMapping("/order/show-order/{id}")
    public R<Void> setShowOrder(@PathVariable Long id, @RequestParam Integer showOrder) {
        Order order = orderService.getById(id);
        if (order == null) return R.error("order not found");
        order.setShowOrder(showOrder);
        orderService.updateById(order);
        return R.success();
    }

    @PostMapping("/order/featured-photos/{id}")
    public R<Void> saveFeaturedPhotos(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Order order = orderService.getById(id);
        if (order == null) return R.error("order not found");
        String photos = (String) body.get("photos");
        order.setFeaturedPhotos(photos);
        order.setIsPhotoFeatured(photos != null && !photos.isEmpty() ? 1 : 0);
        orderService.updateById(order);
        return R.success();
    }

    @GetMapping("/orders/photo-featured")
    public R<List<Map<String, Object>>> getPhotoFeaturedOrders() {
        List<Order> orders = orderService.list(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getIsPhotoFeatured, 1)
                        .eq(Order::getStatus, OrderStatus.COMPLETED)
                        .orderByAsc(Order::getShowOrder));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order o : orders) {
            Map<String, Object> item = new HashMap<>();
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
