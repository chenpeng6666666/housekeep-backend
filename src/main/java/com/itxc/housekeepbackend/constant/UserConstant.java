package com.itxc.housekeepbackend.constant;

public interface UserConstant {

    /**
     * 密码加盐
     */
    String PASSWORD_PRE = "pwd";

    /**
     * 默认密码(存入数据库时不需要加盐)
     */
    String DEFAULT_PWD = "pwd123456";


    // 全局用户身份
    public static final String USER = "user";
    public static final String COMPANY = "company";
    public static final String ADMIN = "admin";




}
