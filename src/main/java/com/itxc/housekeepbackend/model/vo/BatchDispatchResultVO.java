package com.itxc.housekeepbackend.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class BatchDispatchResultVO {
    private int successCount;
    private int failCount;
    private List<String> failMessages; // 记录失败的具体原因（如：ORD1234 无可用阿姨）
}