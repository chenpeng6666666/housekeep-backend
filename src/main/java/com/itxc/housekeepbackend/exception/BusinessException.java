package com.itxc.housekeepbackend.exception;

import lombok.Getter;

/**
 * @author Xy
 * @version 1.0
 * @description: 自定义业务异常
 * @date 2026/3/18 19:22
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }




}
