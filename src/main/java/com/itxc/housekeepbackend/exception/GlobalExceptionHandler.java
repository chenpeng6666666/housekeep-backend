package com.itxc.housekeepbackend.exception;

import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Xy
 * @version 1.0
 * @description: 全局异常处理器
 * @date 2026/3/18 19:51
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(value = BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException:", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    public BaseResponse<?> businessExceptionHandler(RuntimeException e) {
        log.error("RuntimeException:", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, e.getMessage());
    }

}
