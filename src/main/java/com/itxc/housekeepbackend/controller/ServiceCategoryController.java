package com.itxc.housekeepbackend.controller;

import cn.hutool.core.util.ObjUtil;
import com.itxc.housekeepbackend.annotation.CheckSysAdmin;
import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.model.entity.ServiceCategory;
import com.itxc.housekeepbackend.service.ServiceCategoryService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author Xy
 * @version 1.0
 * @description: TODO 服务分类接口
 * @date 2026/4/5 16:39
 */
@RestController
@RequestMapping("/category")
public class ServiceCategoryController {

    @Resource
    private ServiceCategoryService serviceCategoryService;

    /**
     * 新增分类
     */
    @PostMapping
    @CheckSysAdmin
    public BaseResponse<String> addCategory(@RequestBody ServiceCategory serviceCategory){
        // 1 参数校验
        ThrowUtils.throwIf(ObjUtil.isNull(serviceCategory), ErrorCode.PARAMS_ERROR);
        // 2 执行新增
        serviceCategoryService.save(serviceCategory);
        return ResultUtils.success("新增分类成功");
    }


    /**
     * 修改分类
     */
    @PutMapping
    @CheckSysAdmin
    public BaseResponse<String> updateCategory(@RequestBody ServiceCategory serviceCategory){
        // 1 参数校验
        ThrowUtils.throwIf(ObjUtil.isNull(serviceCategory), ErrorCode.PARAMS_ERROR);
        // 2 执行修改
        serviceCategoryService.updateById(serviceCategory);
        return ResultUtils.success("修改分类成功");
    }


    /**
     * 查询分类详情
     */


    /**
     * 查询分类下所关联的服务列表
     */


    /**
     * 分类详情分页查询（admin）
     */


    /**
     * 查询分类列表
     */



}
