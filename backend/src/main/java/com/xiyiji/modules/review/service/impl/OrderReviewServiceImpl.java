package com.xiyiji.modules.review.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiyiji.modules.order.entity.Order;
import com.xiyiji.modules.order.mapper.OrderMapper;
import com.xiyiji.modules.review.entity.OrderReview;
import com.xiyiji.modules.review.mapper.OrderReviewMapper;
import com.xiyiji.modules.review.service.OrderReviewService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OrderReviewServiceImpl extends ServiceImpl<OrderReviewMapper, OrderReview> implements OrderReviewService {

    @Resource
    private OrderReviewMapper orderReviewMapper;

    @Resource
    private OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderReview submitReview(Long orderId, Long userId, Integer score, String content) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!userId.equals(order.getUserId())) {
            throw new RuntimeException("无权评价该订单");
        }
        if (order.getStatus() != 3) {
            throw new RuntimeException("订单未完成，无法评价");
        }
        if (hasReview(orderId)) {
            throw new RuntimeException("该订单已评价");
        }

        OrderReview review = new OrderReview();
        review.setOrderId(orderId);
        review.setUserId(userId);
        review.setScore(score);
        review.setContent(content);
        review.setCreateTime(LocalDateTime.now());

        orderReviewMapper.insert(review);
        return review;
    }

    @Override
    public OrderReview getReviewByOrderId(Long orderId) {
        return orderReviewMapper.selectByOrderId(orderId);
    }

    @Override
    public boolean hasReview(Long orderId) {
        return orderReviewMapper.countByOrderId(orderId) > 0;
    }
}
