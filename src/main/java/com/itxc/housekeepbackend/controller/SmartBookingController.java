package com.itxc.housekeepbackend.controller;

import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.model.dto.SmartBookingRequestDTO;
import com.itxc.housekeepbackend.model.vo.SmartMatchResultVO;
import com.itxc.housekeepbackend.service.SmartBookingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 智慧服务预约控制器
 */
@RestController
@RequestMapping("/smart")
public class SmartBookingController {

    @Resource
    private SmartBookingService smartBookingService;

    /**
     * 多模态智能预约服务匹配
     *
     * @param requestDTO 智慧预约请求参数
     * @return 智能匹配出的标准服务和建议内容
     */
    @PostMapping("/match")
    public BaseResponse<SmartMatchResultVO> matchService(@RequestBody SmartBookingRequestDTO requestDTO) {
        SmartMatchResultVO result = smartBookingService.matchService(requestDTO);
        return ResultUtils.success(result);
    }
}
