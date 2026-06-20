package com.itxc.housekeepbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.itxc.housekeepbackend.mapper.ServiceItemMapper;
import com.itxc.housekeepbackend.model.dto.SmartBookingRequestDTO;
import com.itxc.housekeepbackend.model.entity.ServiceItem;
import com.itxc.housekeepbackend.model.vo.SmartMatchResultVO;
import com.itxc.housekeepbackend.service.SmartBookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class SmartBookingServiceImpl implements SmartBookingService {

    @Resource
    private ServiceItemMapper serviceItemMapper;

    @Override
    public SmartMatchResultVO matchService(SmartBookingRequestDTO requestDTO) {
        SmartMatchResultVO result = new SmartMatchResultVO();
        String text = requestDTO.getText();
        String imageUrl = requestDTO.getImageUrl();

        // 设置默认值
        result.setQuantity(1);
        result.setKeyword("普通保洁");
        result.setRemark(text);

        if (StringUtils.isBlank(text) && StringUtils.isBlank(imageUrl)) {// 如果用户提交的预约内容的都是空的
            // 返回默认的保洁服务
            setDefaultService(result);
            return result;
        }

        // 解析关键字
        String keyword = "";
        if (StringUtils.isNotBlank(text)) {// 先进行关键词匹配
            if (text.contains("玻璃")) {
                keyword = "玻璃";
            } else if (text.contains("深度") || text.contains("深层") || text.contains("彻底") || text.contains("精细")) {
                keyword = "深度";
            } else if (text.contains("开荒") || text.contains("装修")) {
                keyword = "开荒";
            } else if (text.contains("家电") || text.contains("空调") || text.contains("清洗") || text.contains("油烟机") || text.contains("洗")) {
                keyword = "清洗";
            } else if (text.contains("做饭")) {
                keyword = "饭";
            } else if (text.contains("保姆") || text.contains("月嫂") || text.contains("育儿")) {
                keyword = "嫂";
            } else if (text.contains("收纳") || text.contains("整理")) {
                keyword = "收纳";
            } else if (text.contains("搬家")) {
                keyword = "搬";
            }
        } else if (StringUtils.isNotBlank(imageUrl)) {
            // 如果只有图片 模拟AI识别出房间脏乱需要保洁
            keyword = "保洁";
            result.setRemark("【系统识别】：基于您上传的场景照片，系统智能诊断为保洁需求，已为您匹配最优质的日常保洁服务。");
        }

        // 去数据库模糊匹配服务
        LambdaQueryWrapper<ServiceItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ServiceItem::getStatus, 1);
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.like(ServiceItem::getName, keyword);
        }
        List<ServiceItem> serviceItems = serviceItemMapper.selectList(queryWrapper);

        ServiceItem matchedItem = null;
        if (!serviceItems.isEmpty()) {
            matchedItem = serviceItems.get(0);
        } else {
            // 如果没有根据关键字匹配到，尝试搜索"日常保洁"或"保洁"
            LambdaQueryWrapper<ServiceItem> backupWrapper = new LambdaQueryWrapper<>();
            backupWrapper.eq(ServiceItem::getStatus, 1);
            backupWrapper.like(ServiceItem::getName, "保洁");
            List<ServiceItem> backupItems = serviceItemMapper.selectList(backupWrapper);
            if (!backupItems.isEmpty()) {
                matchedItem = backupItems.get(0);
            } else {
                // 如果还匹配不到，查询全部上架服务取第一个作为默认
                LambdaQueryWrapper<ServiceItem> allWrapper = new LambdaQueryWrapper<>();
                allWrapper.eq(ServiceItem::getStatus, 1);
                List<ServiceItem> allItems = serviceItemMapper.selectList(allWrapper);
                if (!allItems.isEmpty()) {
                    matchedItem = allItems.get(0);
                }
            }
        }

        if (matchedItem != null) {
            result.setServiceId(matchedItem.getId());
            result.setServiceName(matchedItem.getName());
            result.setKeyword(matchedItem.getName());
        }

        // 解析数量 (比如：80平米，2小时)
        if (StringUtils.isNotBlank(text)) {
            Pattern pattern = Pattern.compile("(\\d+)\\s*(平米|平方米|㎡|小时|个|台|只|次|份)");
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                try {
                    int quantity = Integer.parseInt(matcher.group(1));
                    result.setQuantity(quantity);
                } catch (NumberFormatException e) {
                    log.error("解析数量失败", e);
                }
            }
        }

        // 构建更智能的备注
        StringBuilder remarkBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(text)) {
            remarkBuilder.append(text);
        }
        if (StringUtils.isNotBlank(imageUrl)) {
            if (remarkBuilder.length() > 0) {
                remarkBuilder.append("；（场景图片已上传）");
            } else {
                remarkBuilder.append("场景图片已上传，请查看。");
            }
        }
        result.setRemark(remarkBuilder.toString());

        return result;
    }

    private void setDefaultService(SmartMatchResultVO result) {
        LambdaQueryWrapper<ServiceItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ServiceItem::getStatus, 1);
        List<ServiceItem> allItems = serviceItemMapper.selectList(queryWrapper);
        if (!allItems.isEmpty()) {
            ServiceItem defaultItem = allItems.get(0);
            result.setServiceId(defaultItem.getId());
            result.setServiceName(defaultItem.getName());
            result.setKeyword(defaultItem.getName());
        }
    }
}
