package com.itxc.housekeepbackend.utils;

import com.itxc.housekeepbackend.enums.ImageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    @Autowired
    private StorageProperties storageProperties;

    @Override
    public String upload(byte[] content, String originalFilename, ImageType imageType) throws Exception {
        String dir = imageType.getDir() + "/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID() + ext;

        Path uploadPath = Paths.get(storageProperties.getLocalBasePath(), dir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        byte[] uploadContent = ImageCompressUtil.compress(content, originalFilename, storageProperties);

        Path filePath = uploadPath.resolve(newFileName);
        Files.write(filePath, uploadContent);

        String url = storageProperties.getLocalAccessUrl() + "/" + dir + "/" + newFileName;
        log.info("本地存储成功: {} -> {}", originalFilename, url);
        return url;
    }
}
