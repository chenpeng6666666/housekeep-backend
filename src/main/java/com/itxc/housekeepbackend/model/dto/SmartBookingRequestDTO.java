package com.itxc.housekeepbackend.model.dto;

import lombok.Data;

@Data
public class SmartBookingRequestDTO {
    /**
     * 用户输入的自然语言文本需求
     */
    private String text;

    /**
     * 用户上传的现场环境图URL
     */
    private String imageUrl;
}
