package com.itxc.housekeepbackend.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class AiChatSessionVO {
    private String sessionId;
    private String title;
    private Date createTime;
}
