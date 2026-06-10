package com.xiyiji.modules.employee.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("employee_application")
public class EmployeeApplication {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;
    private String phone;
    private Integer status; // 0-待审核, 1-已通过, 2-已拒绝
    private String remark;
    private Long handlerId;
    private LocalDateTime handleTime;
    private LocalDateTime createTime;
}