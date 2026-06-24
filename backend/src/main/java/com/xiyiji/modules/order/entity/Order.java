package com.xiyiji.modules.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private String userName;
    private String buildingCategory;
    private String buildingName;
    private String roomNo;
    private String contactPhone;
    private LocalDate serviceDate;
    private String startTime;
    private String endTime;
    private Integer status;
    private Long employeeId;
    private BigDecimal amount;
    private String remark;
    private String beforePhoto;
    private String afterPhoto;
    /** 是否展示照片：0-否，1-是 */
    private Integer isPhotoFeatured;
    /** 展示顺序：0不展示，数字越小越靠前 */
    private Integer showOrder;
    /** 展示的照片列表（JSON数组） */
    private String featuredPhotos;
    private LocalDateTime createTime;
    private LocalDateTime payTime;
    private LocalDateTime completeTime;
}
