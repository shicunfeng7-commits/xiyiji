package com.xiyiji.common.constant;

public interface OrderStatus {
    int UNPAID = 0;              // 未支付
    int PAID = 1;                // 已支付
    int IN_PROGRESS = 2;         // 服务中
    int COMPLETED = 3;           // 已完成
    int CANCELLED = 4;           // 已取消
    int PENDING_SERVICE = 5;     // 待服务（员工已抢单，等待开始）
}
