package com.xiyiji.modules.employee.controller;

import com.xiyiji.common.result.R;
import com.xiyiji.modules.employee.entity.Employee;
import com.xiyiji.modules.employee.service.EmployeeService;
import com.xiyiji.modules.order.entity.Order;
import com.xiyiji.modules.order.service.OrderService;
import com.xiyiji.modules.review.entity.OrderReview;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/employee")
@Tag(name = "")
public class EmployeeController {

    @Resource
    private HttpServletRequest request;

    @Resource
    private EmployeeService employeeService;

    @Resource
    private OrderService orderService;

    @Resource
    private com.xiyiji.modules.review.service.OrderReviewService orderReviewService;

    @Resource
    private com.xiyiji.common.config.UploadProperties uploadProperties;

    @Operation(summary = "")
    @GetMapping("/info")
    public R<Employee> getInfo() {
        Long userId = (Long) request.getAttribute("userId");
        Employee employee = employeeService.getByUserId(userId);
        if (employee != null) {
            return R.success(employee);
        }
        return R.error("");
    }

    @GetMapping("/orders/available")
    public R<List<Order>> getAvailableOrders() {
        return R.success(orderService.getAvailableOrders());
    }

    @Operation(summary = "")
    @PostMapping("/order/grab/{orderId}")
    public R<Void> grabOrder(@PathVariable Long orderId) {
        Long userId = (Long) request.getAttribute("userId");
        Employee employee = employeeService.getByUserId(userId);
        if (employee == null) {
            return R.error("");
        }
        if (employee.getIsActive() == null || employee.getIsActive() != 1) {
            return R.error("");
        }
        boolean success = orderService.grabOrder(orderId, employee.getId());
        return success ? R.success() : R.error("");
    }

    @GetMapping("/orders/my-list")
    public R<List<Order>> getMyOrders() {
        Long userId = (Long) request.getAttribute("userId");
        Employee employee = employeeService.getByUserId(userId);
        if (employee == null) {
            return R.error("");
        }
        return R.success(orderService.getEmployeeOrders(employee.getId()));
    }

    @PostMapping("/order/start/{orderId}")
    public R<Void> startOrder(@PathVariable Long orderId) {
        Long userId = (Long) request.getAttribute("userId");
        Employee employee = employeeService.getByUserId(userId);
        if (employee == null) {
            return R.error("");
        }
        boolean success = orderService.startOrder(orderId, employee.getId());
        return success ? R.success() : R.error("");
    }

    @Operation(summary = "")
    @PostMapping("/order/complete/{orderId}")
    public R<Void> completeOrder(@PathVariable Long orderId,
                                  @RequestParam(required = false) String beforePhotos,
                                  @RequestParam(required = false) String afterPhotos) {
        Long userId = (Long) request.getAttribute("userId");
        Employee employee = employeeService.getByUserId(userId);
        if (employee == null) {
            return R.error("");
        }
        if (beforePhotos == null || afterPhotos == null || beforePhotos.isEmpty() || afterPhotos.isEmpty()) {
            return R.error("");
        }
        boolean success = orderService.completeOrder(orderId, employee.getId(), beforePhotos, afterPhotos);
        return success ? R.success() : R.error("");
    }

    @PostMapping("/upload-photo")
    public R<String> uploadPhoto(@RequestBody Map<String, String> body) {
        String base64 = body.get("image");
        if (base64 == null || base64.isEmpty()) {
            return R.error("");
        }
        try {
            java.io.File uploadDir = new java.io.File(uploadProperties.getDir());
            if (!uploadDir.exists()) uploadDir.mkdirs();
            String filename = System.currentTimeMillis() + ".jpg";
            java.io.File file = new java.io.File(uploadDir, filename);
            byte[] bytes = java.util.Base64.getDecoder().decode(base64);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
            fos.write(bytes);
            fos.close();
            return R.success("/uploads/" + filename);
        } catch (Exception e) {
            return R.error(": " + e.getMessage());
        }
    }

    @Operation(summary = "")
    @GetMapping("/stats")
    public R<Map<String, Object>> getStats(
            @RequestParam(required = false, defaultValue = "today") String range) {
        Long userId = (Long) request.getAttribute("userId");
        Employee employee = employeeService.getByUserId(userId);
        if (employee == null) return R.error("");

        LocalDate start;
        LocalDate end = LocalDate.now();
        switch (range) {
            case "week":
                start = end.with(DayOfWeek.MONDAY);
                break;
            case "month":
                start = end.withDayOfMonth(1);
                break;
            default:
                start = end;
                break;
        }

        var query = orderService.lambdaQuery()
                .eq(Order::getEmployeeId, employee.getId())
                .eq(Order::getStatus, com.xiyiji.common.constant.OrderStatus.COMPLETED)
                .ge(Order::getCompleteTime, start.atStartOfDay());
        if (range.equals("today")) {
            query.le(Order::getCompleteTime, end.plusDays(1).atStartOfDay());
        }
        List<Order> orders = query.list();

        BigDecimal totalRevenue = orders.stream()
                .map(o -> o.getAmount() != null ? o.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> result = new HashMap<>();
        result.put("completedCount", orders.size());
        result.put("totalRevenue", totalRevenue);
        result.put("range", range);
        return R.success(result);
    }

    @Operation(summary = "")
    @GetMapping("/order/review/{orderId}")
    public R<OrderReview> getOrderReview(@PathVariable Long orderId) {
        OrderReview review = orderReviewService.getReviewByOrderId(orderId);
        return R.success(review);
    }
}