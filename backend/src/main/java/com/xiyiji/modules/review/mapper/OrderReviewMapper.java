package com.xiyiji.modules.review.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiyiji.modules.review.entity.OrderReview;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderReviewMapper extends BaseMapper<OrderReview> {

    OrderReview selectByOrderId(@Param("orderId") Long orderId);

    int countByOrderId(@Param("orderId") Long orderId);
}
