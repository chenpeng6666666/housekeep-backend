package com.itxc.housekeepbackend.controller.common;

import com.itxc.housekeepbackend.annotation.RequireAuth;
import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.enums.ImageType;
import com.itxc.housekeepbackend.exception.ErrorCode;
import com.itxc.housekeepbackend.exception.ThrowUtils;
import com.itxc.housekeepbackend.model.entity.User;
import com.itxc.housekeepbackend.service.SysAdminService;
import com.itxc.housekeepbackend.service.UserService;
import com.itxc.housekeepbackend.utils.AliyunOSSOperator;
import com.itxc.housekeepbackend.utils.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/upload")
public class UploadController {

    @Autowired
    private StorageService storageService;

    @Resource
    private UserService userService;

    @Resource
    private SysAdminService sysAdminService;

    @PostMapping("/avatar")
    public BaseResponse<String> uploadAvatar(MultipartFile file) throws Exception {
        return doUpload(file, ImageType.AVATAR, "用户头像");
    }

    @PostMapping("/category")
//    @RequireAuth(value = "admin")
    public BaseResponse<String> uploadCategory(MultipartFile file) throws Exception {
        return doUpload(file, ImageType.CATEGORY, "服务分类图片");
    }

    @PostMapping("/service")
//    @RequireAuth(value = "company")
    public BaseResponse<String> uploadService(MultipartFile file) throws Exception {
        return doUpload(file, ImageType.SERVICE, "服务图片");
    }

    @PostMapping("/company")
//    @RequireAuth(value = "company")
    public BaseResponse<String> uploadCompany(MultipartFile file) throws Exception {
        return doUpload(file, ImageType.COMPANY, "企业logo");
    }

    private BaseResponse<String> doUpload(MultipartFile file, ImageType type, String desc) throws Exception {
        log.info("{}上传: {}", desc, file.getOriginalFilename());
        String url = storageService.upload(file.getBytes(), file.getOriginalFilename(), type);
        log.info("{}上传成功, url: {}", desc, url);
        return ResultUtils.success(url);
    }

}
