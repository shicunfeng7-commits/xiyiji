package com.xiyiji.common.constant;

public interface OrderStatus {
    int UNPAID = 0;         // 未支付
    int PAID = 1;           // 已支付待服务
    int IN_PROGRESS = 2;    // 服务中
    int COMPLETED = 3;      // 已完成
    int CANCELLED = 4;      // 已取消
}