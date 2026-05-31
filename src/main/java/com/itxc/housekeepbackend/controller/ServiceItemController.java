package com.itxc.housekeepbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itxc.housekeepbackend.annotation.RequireAuth;
import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.model.dto.serviceItem.ServiceItemAddRequest;
import com.itxc.housekeepbackend.model.dto.serviceItem.ServiceItemQueryRequest;
import com.itxc.housekeepbackend.model.entity.ServiceItem;
import com.itxc.housekeepbackend.service.ServiceItemService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

import static com.itxc.housekeepbackend.constant.UserConstant.ADMIN;

@RestController
@RequestMapping("/service-item")
public class ServiceItemController {

    @Resource
    private ServiceItemService serviceItemService;

    /**
     * 分页查询服务项目
     */
    @GetMapping("/page")
    public BaseResponse<Page<ServiceItem>> page(ServiceItemQueryRequest request) {
        Page<ServiceItem> pageResult = serviceItemService.pageQuery(request);
        return ResultUtils.success(pageResult);
    }

    /**
     * 新增服务项目
     */
    @PostMapping("/add")
    @RequireAuth(ADMIN)
    public BaseResponse<Boolean> add(@RequestBody ServiceItemAddRequest request) {
        ServiceItem serviceItem = new ServiceItem();
        BeanUtils.copyProperties(request, serviceItem);
        // 默认状态为上架
        serviceItem.setStatus(1); 
        boolean result = serviceItemService.save(serviceItem);
        return ResultUtils.success(result);
    }

    /**
     * 修改服务项目 (包含修改信息 和 状态上下架)
     */
    @PutMapping("/update")
    @RequireAuth(ADMIN)
    public BaseResponse<Boolean> update(@RequestBody ServiceItemAddRequest request) {
        ThrowUtils.throwIf(request.getId() == null, ErrorCode.PARAMS_ERROR);
        ServiceItem serviceItem = new ServiceItem();
        BeanUtils.copyProperties(request, serviceItem);
        
        boolean result = serviceItemService.updateById(serviceItem);
        return ResultUtils.success(result);
    }

    /**
     * 删除服务项目
     */
    @DeleteMapping("/delete/{id}")
    @RequireAuth(ADMIN)
    public BaseResponse<Boolean> delete(@PathVariable Long id) {
        boolean result = serviceItemService.removeById(id);
        return ResultUtils.success(result);
    }


}