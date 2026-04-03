package com.itxc.housekeepbackend.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的删除请求类
 *
 * @author Xy
 * @date 2026/3/18 19:37
 */
@Data
public class DeleteRequest implements Serializable {
    /**
     * id
     */
    private int id;

    private static final long serialVersionUID = 1L;
}