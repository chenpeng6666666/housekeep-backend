package com.itxc.housekeepbackend.controller.company;

import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.model.dto.user.UserLoginDto;
import com.itxc.housekeepbackend.model.vo.EmployeeVO;
import com.itxc.housekeepbackend.service.CompanyEmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {

    @Resource
    private CompanyEmployeeService companyEmployeeService;

    /**
     * 企业员工登录
     */
    @PostMapping("/login")
    public BaseResponse<EmployeeVO> employeeLogin(@RequestBody UserLoginDto userLoginDto, HttpServletRequest request) {
        // 1 TODO 参数校验
        EmployeeVO employeeVO = companyEmployeeService.login(userLoginDto, request);
        return ResultUtils.success(employeeVO);
    }
}
