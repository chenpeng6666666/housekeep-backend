package com.itxc.housekeepbackend.controller;

import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Xy
 * @version 1.0
 * @description: 健康测试
 * @date 2026/4/3 16:36
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public BaseResponse<String> heath(){
        return ResultUtils.success("health");
    }


}
