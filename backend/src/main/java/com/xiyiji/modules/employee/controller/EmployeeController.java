package com.xiyiji.modules.employee.controller;

import com.xiyiji.common.result.R;
import com.xiyiji.modules.employee.entity.Employee;
import com.xiyiji.modules.employee.service.EmployeeService;
import com.xiyiji.modules.order.entity.Order;
import com.xiyiji.modules.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@Tag(name = "员工端接口", description = "抢单、开始服务、完成服务、员工统计")
public class EmployeeController {

    @Resource
    private HttpServletRequest request;

    @Resource
    private EmployeeService employeeService;

    @Resource
    private OrderService orderService;

    @Operation(summary = "获取当前员工信息")
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

    @Operation(summary = "抢单")
    @PostMapping("/order/grab/{orderId}")
    public R<Void> grabOrder(@PathVariable Long orderId) {
        Long userId = (Long) request.getAttribute("userId");
        Employee employee = employeeService.getByUserId(userId);
        if (employee == null) {
            return R.error("该用户不是员工");
        }
        if (employee.getIsActive() == null || employee.getIsActive() == com.xiyiji.common.constant.EmployeeStatus.DISABLED) {
            return R.error("该员工已被停用，无法抢单");
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

    @Operation(summary = "完成服务（需上传清洗前后照片）")
    @PostMapping("/order/complete/{orderId}")
    public R<Void> completeOrder(@PathVariable Long orderId,
                                  @RequestParam(required = false) String beforePhotos,
                                  @RequestParam(required = false) String afterPhotos) {
        Long userId = (Long) request.getAttribute("userId");
        Employee employee = employeeService.getByUserId(userId);
        if (employee == null) {
            return R.error("该用户不是员工");
        }
        if (beforePhotos == null || afterPhotos == null || beforePhotos.isEmpty() || afterPhotos.isEmpty()) {
            return R.error("请至少上传一张清洗前和清洗后的照片");
        }
        boolean success = orderService.completeOrder(orderId, employee.getId(), beforePhotos, afterPhotos);
        return success ? R.success() : R.error("完成服务失败，订单状态异常");
    }

    /**
     * 上传照片（Base64）
     */
    @PostMapping("/upload-photo")
    public R<String> uploadPhoto(@RequestBody java.util.Map<String, String> body) {
        String base64 = body.get("image");
        if (base64 == null || base64.isEmpty()) {
            return R.error("图片数据为空");
        }
        try {
            java.io.File uploadDir = new java.io.File("uploads");
            if (!uploadDir.exists()) uploadDir.mkdirs();
            String filename = System.currentTimeMillis() + ".jpg";
            java.io.File file = new java.io.File(uploadDir, filename);
            byte[] bytes = java.util.Base64.getDecoder().decode(base64);
            java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
            fos.write(bytes);
            fos.close();
            return R.success("/uploads/" + filename);
        } catch (Exception e) {
            return R.error("图片保存失败: " + e.getMessage());
        }
    }

    @Operation(summary = "员工统计（今日/本周/本月完成订单数和收入）")
    @GetMapping("/stats")
    public R<java.util.Map<String, Object>> getStats(
            @RequestParam(required = false, defaultValue = "today") String range) {
        Long userId = (Long) request.getAttribute("userId");
        Employee employee = employeeService.getByUserId(userId);
        if (employee == null) return R.error("该用户不是员工");

        java.time.LocalDate start;
        java.time.LocalDate end = java.time.LocalDate.now();
        switch (range) {
            case "week":
                start = end.with(java.time.DayOfWeek.MONDAY);
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
        java.util.List<Order> orders = query.list();

        java.math.BigDecimal totalRevenue = orders.stream()
                .map(o -> o.getAmount() != null ? o.getAmount() : java.math.BigDecimal.ZERO)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("completedCount", orders.size());
        result.put("totalRevenue", totalRevenue);
        result.put("range", range);
        return R.success(result);
    }
}