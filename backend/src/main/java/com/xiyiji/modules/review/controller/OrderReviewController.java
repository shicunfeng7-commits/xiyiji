package com.xiyiji.modules.review.controller;

import com.xiyiji.common.result.R;
import com.xiyiji.modules.review.entity.OrderReview;
import com.xiyiji.modules.review.service.OrderReviewService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/order")
public class OrderReviewController {

    @Resource
    private OrderReviewService orderReviewService;

    @PostMapping("/review")
    public R<OrderReview> submitReview(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        Long orderId = Long.parseLong(body.get("orderId").toString());
        Integer score = Integer.parseInt(body.get("score").toString());
        String content = (String) body.get("content");

        OrderReview review = orderReviewService.submitReview(orderId, userId, score, content);
        return R.success(review);
    }

    @GetMapping("/review/{orderId}")
    public R<OrderReview> getReview(@PathVariable Long orderId) {
        OrderReview review = orderReviewService.getReviewByOrderId(orderId);
        return R.success(review);
    }

    @GetMapping("/review/check/{orderId}")
    public R<Boolean> hasReview(@PathVariable Long orderId) {
        boolean hasReview = orderReviewService.hasReview(orderId);
        return R.success(hasReview);
    }
}
