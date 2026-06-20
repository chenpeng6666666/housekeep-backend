package com.itxc.housekeepbackend.service;

import com.itxc.housekeepbackend.model.dto.SmartBookingRequestDTO;
import com.itxc.housekeepbackend.model.vo.SmartMatchResultVO;

public interface SmartBookingService {

    /**
     * 多模态智慧服务匹配
     * @param requestDTO 包含文本或图片的请求
     * @return 匹配结果
     */
    SmartMatchResultVO matchService(SmartBookingRequestDTO requestDTO);
}
