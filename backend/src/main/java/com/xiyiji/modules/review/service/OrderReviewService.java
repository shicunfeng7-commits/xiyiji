package com.xiyiji.modules.review.service;

import com.xiyiji.modules.review.entity.OrderReview;

public interface OrderReviewService {

    OrderReview submitReview(Long orderId, Long userId, Integer score, String content);

    OrderReview getReviewByOrderId(Long orderId);

    boolean hasReview(Long orderId);
}
