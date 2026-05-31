package com.itxc.housekeepbackend.exception;

/**
 * @author Xy
 * @version 1.0
 * @description: 异常处理工具类
 * @date 2026/3/18 19:28
 */
public class ThrowUtils {

    /**
     * 如果条件成立则抛出异常
     * @param condition
     * @param runtimeException
     */
    public static void throwIf(boolean condition, RuntimeException runtimeException) {
        if (condition) {
            throw runtimeException;
        }
    }

    /**
     * 如果条件成立则抛出异常
     * @param condition
     * @param errorCode
     */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }

    /**
     * 如果条件成立则抛出异常
     * @param condition
     * @param errorCode
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }


}
