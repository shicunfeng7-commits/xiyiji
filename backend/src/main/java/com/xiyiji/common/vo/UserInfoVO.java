package com.xiyiji.common.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户信息")
public class UserInfoVO {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "宿舍楼栋")
    private String buildingName;

    @Schema(description = "房间号")
    private String roomNo;

    @Schema(description = "角色：admin/employee/user")
    private String role;

    @Schema(description = "员工ID（仅员工有值）")
    private Long employeeId;
}
