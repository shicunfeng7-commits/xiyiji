package com.xiyiji.modules.review.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_review")
public class OrderReview {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private Long orderId;

    @TableField("user_id")
    private Long userId;

    @TableField("score")
    private Integer score;

    @TableField("content")
    private String content;

    /** 是否精选展示：0-否，1-是 */
    @TableField("is_featured")
    private Integer isFeatured;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String phone;

    @TableField(exist = false)
    private String nickname;
}
