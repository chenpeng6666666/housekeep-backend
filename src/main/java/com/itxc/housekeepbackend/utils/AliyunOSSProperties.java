package com.itxc.housekeepbackend.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliyunOSSProperties {
    private String endpoint;
    private String bucketName;
    private String region;


    private boolean enableCompress = true;
    private int maxWidth = 1920;
    private float compressQuality = 0.75f;
    private long compressThreshold = 200 * 1024;
}
