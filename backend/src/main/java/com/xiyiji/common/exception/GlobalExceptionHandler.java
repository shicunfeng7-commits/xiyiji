package com.xiyiji.common.exception;

import com.xiyiji.common.result.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 作用：统一处理项目中所有报错，返回固定格式的错误信息给前端
 * 前端用户看到的是人话（如"服务器内部错误"），而不是一堆代码报错
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 兜底处理：不管什么类型的报错，都交给这个方法处理
     * 比喻：医院前台，所有病人都先到这登记
     *
     * @param e 异常对象，包含错误信息
     * @return 返回 500 状态码 + 通用错误提示
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<R<Void>> handleException(Exception e) {
        log.error("服务器内部错误", e);  // 写日志，只有开发者能看到，用于排查问题
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(R.error("服务器内部错误"));  // 返回给人话，不暴露代码细节
    }

    /**
     * 处理参数校验异常：前端传了不合法的参数（如必填字段为空）
     * 比喻：病人说的症状是"头疼"，前台就知道挂哪个科
     *
     * @param e 参数异常对象，getMessage() 返回具体的错误信息（如"手机号不能为空"）
     * @return 返回 400 状态码 + 具体的参数错误信息
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<R<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数异常: {}", e.getMessage());  // warn 级别，比 error 轻
        return ResponseEntity.badRequest().body(R.error(e.getMessage()));  // badRequest = 400
    }

    /**
     * 处理运行时异常：程序运行过程中出的错（如空指针、类型转换失败）
     *
     * @param e 运行时异常对象
     * @return 返回 500 状态码 + 异常的具体信息
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<R<Void>> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常", e);  // 写日志，带完整堆栈信息，方便排查
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(R.error(e.getMessage()));  // 返回异常信息给前端
    }

    /**
     * 处理业务异常：业务逻辑不允许的操作（如"该时间段已有订单"）
     * 这是我们自己定义的异常，专门用来处理业务层面的错误
     *
     * @param e 业务异常对象，包含自定义的状态码和错误信息
     * @return 返回自定义的状态码 + 业务错误信息
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<Void>> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return ResponseEntity.status(e.getCode())
                .body(R.error(e.getCode(), e.getMessage()));
    }
}
