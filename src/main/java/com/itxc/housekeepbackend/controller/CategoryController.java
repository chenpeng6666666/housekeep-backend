package com.itxc.housekeepbackend.controller;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itxc.housekeepbackend.annotation.RequireAuth;
import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.model.entity.ServiceCategory;
import com.itxc.housekeepbackend.model.entity.ServiceItem;
import com.itxc.housekeepbackend.model.entity.SysAdmin;
import com.itxc.housekeepbackend.model.entity.User;
import com.itxc.housekeepbackend.model.enums.RoleTypeEnum;
import com.itxc.housekeepbackend.service.ServiceCategoryService;
import com.itxc.housekeepbackend.service.SysAdminService;
import com.itxc.housekeepbackend.service.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.itxc.housekeepbackend.constant.StatusConstant.STATUS_NORMAL;

/**
 * @author Xy
 * @version 1.0
 * @description: TODO 服务分类接口
 * @date 2026/4/5 16:39
 */
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Resource
    private ServiceCategoryService serviceCategoryService;

    @Resource
    private UserService userService;// TODO 企业端

    @Resource
    private SysAdminService sysAdminService;

    /**
     * 新增分类 (管理员权限)
     */
    @PostMapping("/add")
    @RequireAuth("admin")
    public BaseResponse<String> addCategory(@RequestBody ServiceCategory serviceCategory) {
        // 1 参数校验
        ThrowUtils.throwIf(ObjUtil.isNull(serviceCategory), ErrorCode.PARAMS_ERROR);
        // 2 执行新增
        serviceCategoryService.save(serviceCategory);
        return ResultUtils.success("新增分类成功");
    }


    /**
     * 修改分类 (管理员权限)
     */
    @PutMapping("/update")
    @RequireAuth("admin")
    public BaseResponse<String> updateCategory(@RequestBody ServiceCategory serviceCategory) {
        // 1 参数校验
        ThrowUtils.throwIf(ObjUtil.isNull(serviceCategory), ErrorCode.PARAMS_ERROR, "参数异常");
        // 2 执行修改
        serviceCategoryService.updateById(serviceCategory);
        return ResultUtils.success("修改分类成功");
    }


    /**
     * 查询分类详情
     */
    @GetMapping("/{id}")
    public BaseResponse<ServiceCategory> getCategoryById(@PathVariable Long id) {
        // 1 参数校验
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 2 查询数据库
        ServiceCategory serviceCategory = serviceCategoryService.getById(id);
        ThrowUtils.throwIf(ObjUtil.isNull(serviceCategory), ErrorCode.NOT_FOUND_ERROR, "分类不存在");
        return ResultUtils.success(serviceCategory);
    }

    /**
     * TODO 查询分类下所关联的服务列表
     */
    @GetMapping("/{id}/services")
    public BaseResponse<List<ServiceItem>> getCategoryServices(@PathVariable Long id) {
        // 1 参数校验
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
//        List<ServiceItem> list = serviceCategoryService.getServicesById(id);
        return ResultUtils.success(null);
    }

    /**
     * 查询分类列表
     */
    @GetMapping("/list")
    public BaseResponse<List<ServiceCategory>> listCategory(HttpServletRequest request) {
        Page<ServiceCategory> page = getServiceCategoryPage(1, 10000, null, request);
        return ResultUtils.success(page.getRecords());
    }

    /**
     * 分页条件查询
     */
    @GetMapping("/page")
    public BaseResponse<Page<ServiceCategory>> page(
            @RequestParam(value = "current",defaultValue = "1") Integer current,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            HttpServletRequest request) {
        Page<ServiceCategory> page = getServiceCategoryPage(current, pageSize, name, request);
        return ResultUtils.success(page);
    }

    private Page<ServiceCategory> getServiceCategoryPage(Integer current, Integer pageSize, String name, HttpServletRequest request) {
        Page<ServiceCategory> pageInfo = new Page<>(current, pageSize);
        LambdaQueryWrapper<ServiceCategory> queryWrapper = new LambdaQueryWrapper<>();

        // 如果当前登录的用户是管理员则查询所有状态的分类
        Object id = request.getSession().getAttribute("admin");
        SysAdmin admin = sysAdminService.getById((Long) id);
        if (ObjUtil.isNull(admin)){// 普通用户 如果当前登录的用户是非管理员则查询所有状态正常的分类
            queryWrapper.eq(ServiceCategory::getStatus, STATUS_NORMAL);
        }

        // 如果按分类名称搜索
        if (name != null && !name.trim().isEmpty()) {
            queryWrapper.like(ServiceCategory::getName, name);
        }
        // 按排序权重升序，再按创建时间降序
        queryWrapper.orderByAsc(ServiceCategory::getSort).orderByDesc(ServiceCategory::getCreateTime);

        Page<ServiceCategory> page = serviceCategoryService.page(pageInfo, queryWrapper);
        return page;
    }

    /**
     * 删除分类 （管理员权限）
     */
    @DeleteMapping("/delete/{id}")
    @RequireAuth("admin")
    public BaseResponse<String> delete(@PathVariable Integer id){
        // 1 参数校验
        ThrowUtils.throwIf(ObjUtil.isNull(id), ErrorCode.PARAMS_ERROR, "参数异常");
        // 2 查询数据库中该数据是否存在
        ServiceCategory category = serviceCategoryService.getById(id);
        ThrowUtils.throwIf(ObjUtil.isNull(category), ErrorCode.NOT_FOUND_ERROR, "分类不存在");
        // 3 若存在则执行删除
        serviceCategoryService.removeById(category);
        return ResultUtils.success("分类数据删除成功");
    }





}
