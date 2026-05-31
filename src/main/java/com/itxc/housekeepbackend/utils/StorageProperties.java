package com.itxc.housekeepbackend.utils;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    private String type = "local";
    private String localBasePath = "D:/housekeep/uploads";
    private String localAccessUrl = "http://localhost:8081/api/uploads";
    private boolean enableCompress = true;
    private int maxWidth = 1920;
    private float compressQuality = 0.75f;
    private long compressThreshold = 200 * 1024;
}