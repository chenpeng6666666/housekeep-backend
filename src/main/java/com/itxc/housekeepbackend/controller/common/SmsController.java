package com.itxc.housekeepbackend.controller.common;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.itxc.housekeepbackend.common.BaseResponse;
import com.itxc.housekeepbackend.common.ResultUtils;
import com.itxc.housekeepbackend.utils.ValidateCodeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static com.itxc.housekeepbackend.constant.RedisConstants.*;


/**
 * @author Xy
 * @version 1.0
 * @description: 短信验证码服务
 */
@Slf4j
@RestController
@RequestMapping("/sms")
public class SmsController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @GetMapping("/getCode/{phone}")
    public BaseResponse<String> send(@PathVariable String phone) {
        if(StringUtils.isNotEmpty(phone)){
            // 生成随机的4位验证码
            String code = ValidateCodeUtils.generateValidateCode(6).toString();
            log.info("code={}",code);
            stringRedisTemplate.opsForValue().set(REGISTER_CODE_KEY + phone, code, CODE_TTL, TimeUnit.MINUTES);
            return ResultUtils.success("短信发送成功");
        }

        return ResultUtils.success("短信发送失败");
    }

}
