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
    private String periodName;
    private String startTime;
    private String endTime;
    private Boolean enabled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}