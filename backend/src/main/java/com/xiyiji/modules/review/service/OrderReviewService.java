package com.xiyiji.modules.review.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiyiji.modules.review.entity.OrderReview;

public interface OrderReviewService extends IService<OrderReview> {

    OrderReview submitReview(Long orderId, Long userId, Integer score, String content);

    OrderReview getReviewByOrderId(Long orderId);

    boolean hasReview(Long orderId);
}
