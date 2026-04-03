package com.itxc.housekeepbackend.common;

import com.itxc.housekeepbackend.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;


/**
 * @author Xy
 * @version 1.0
 * @description: 全局响应封装类
 * @date 2026/3/18 19:37
 */
@Data
public class BaseResponse<T> implements Serializable {

    // 响应码
    private int code;

    private T data;

    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}
