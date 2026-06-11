package com.xiyiji.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("service_time_config")
public class ServiceTimeConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String period;
    private String periodName;
    private Integer startHour;
    private Integer endHour;
    private Boolean enabled;
    private Integer sortOrder;
    private LocalDateTime createTime;
}