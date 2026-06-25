package com.xiyiji.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "用户手机号登录请求")
public class UserLoginDTO {

    @NotBlank(message = "手机号不能为空")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "验证码（验证码登录时必填）", example = "123456")
    private String code;
}
