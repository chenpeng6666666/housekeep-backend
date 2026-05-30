package com.itxc.housekeepbackend.constant;

/**
 * 工作状态 (1:空闲, 2:服务中, 3:请假)
 */
public interface EmployeeConstant {

    /**
     * 员工状态
     */
    Integer WORK_STATUS_FREE = 1;
    Integer WORK_STATUS_BUSY = 2;
    Integer WORK_STATUS_LEAVE = 3;

    /**
     * 员工角色参数
     */
    public static final String Employee_ADMIN = "ADMIN";
    public static final String Employee_STAFF = "STAFF";


}