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
     * 完成服务（员工只能操作自己的订单，需上传前后对比照片）
     * beforePhotos / afterPhotos 为 JSON 字符串数组，例如：["url1","url2"]
     */
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

    /**
     * 员工统计（今日/本周/本月完成订单数+收入）
     */
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

        java.util.List<Order> orders = orderService.lambdaQuery()
                .eq(Order::getEmployeeId, employee.getId())
                .eq(Order::getStatus, 3)
                .ge(Order::getCompleteTime, start.atStartOfDay())
                .le(range.equals("today") ? Order::getCompleteTime : Order::getId, range.equals("today") ? end.plusDays(1).atStartOfDay() : null)
                .list();

        if (!range.equals("today")) {
            orders = orders.stream()
                    .filter(o -> !o.getCompleteTime().toLocalDate().isBefore(start))
                    .toList();
        }

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