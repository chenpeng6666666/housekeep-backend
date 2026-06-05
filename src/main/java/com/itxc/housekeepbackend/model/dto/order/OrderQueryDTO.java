package com.itxc.housekeepbackend.model.dto.order;

import com.itxc.housekeepbackend.common.PageRequest;
import lombok.Data;

@Data
public class OrderQueryDTO extends PageRequest {

    private String orderNo;
    private Integer status;
    private String startTime; // 预约开始时间
    private String endTime;   // 预约结束时间


}