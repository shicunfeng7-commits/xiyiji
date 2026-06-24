package com.xiyiji.common.exception;

import lombok.Getter;

/**
 * 自定义业务异常类
 * 作用：在业务代码中主动抛出异常，用于处理"业务不允许的操作"
 *
 * 使用示例：
 *   throw new BusinessException("该时间段已有订单，请选择其他时间");
 *   throw new BusinessException(403, "无权限访问");
 *
 * 与 RuntimeException 的区别：
 *   - RuntimeException：程序bug导致的错误（如空指针），不应该出现
 *   - BusinessException：业务逻辑不允许的操作，是预期内的错误
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 错误状态码，如 400 参数错误、403 无权限、404 资源不存在 */
    private final int code;

    /**
     * 使用默认状态码 400（参数错误）
     * @param message 错误信息，如"手机号不能为空"
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    /**
     * 自定义状态码和错误信息
     * @param code HTTP 状态码，如 400、403、404
     * @param message 错误信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
