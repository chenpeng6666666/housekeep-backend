package com.itxc.housekeepbackend.mapper;

import com.itxc.housekeepbackend.model.entity.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
* @author Lenovo
* @description 针对表【order(服务预约订单表)】的数据库操作Mapper
* @createDate 2026-06-01 21:10:37
* @Entity com.itxc.housekeepbackend.model.entity.Order
*/
public interface OrderMapper extends BaseMapper<Order> {



}




