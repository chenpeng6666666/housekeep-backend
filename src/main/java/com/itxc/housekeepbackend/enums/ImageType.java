package com.itxc.housekeepbackend.enums;

public enum ImageType {
    AVATAR("avatar", "用户头像"),
    CATEGORY("category", "服务分类图片"),
    SERVICE("service", "服务图片"),
    COMPANY("company", "企业logo");

    private final String dir;
    private final String desc;

    ImageType(String dir, String desc) {
        this.dir = dir;
        this.desc = desc;
    }

    public String getDir() {
        return dir;
    }

    public String getDesc() {
        return desc;
    }
}