package com.itxc.housekeepbackend.utils;

import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.itxc.housekeepbackend.enums.ImageType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.UUID;

@Slf4j
@Component
public class AliyunOSSOperator {

    @Autowired
    private AliyunOSSProperties aliyunOSSProperties;

    /**
     * 通用上传方法（按图片类型分目录存储，上传前自动压缩）
     */
    public String upload(byte[] content, String originalFilename, ImageType imageType) throws Exception {
        String endpoint = aliyunOSSProperties.getEndpoint();
        String bucketName = aliyunOSSProperties.getBucketName();
        String region = aliyunOSSProperties.getRegion();

        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

        // 按类型分目录：type/yyyy/MM/UUID.ext
        String dir = imageType.getDir() + "/" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String newFileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = dir + "/" + newFileName;

        byte[] uploadContent = compressImage(content, originalFilename);

        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(uploadContent));
        } finally {
            ossClient.shutdown();
        }

        return endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + objectName;
    }

    /**
     * 兼容旧方法（默认存储到 service 目录）
     */
    public String upload(byte[] content, String originalFilename) throws Exception {
        return upload(content, originalFilename, ImageType.SERVICE);
    }

    /**
     * 图片压缩：超过阈值时进行缩放 + JPEG 质量压缩
     */
    private byte[] compressImage(byte[] content, String originalFilename) {
        if (!aliyunOSSProperties.isEnableCompress()) {
            return content;
        }

        if (content.length <= aliyunOSSProperties.getCompressThreshold()) {
            log.info("图片大小 {}KB 未超过阈值 {}KB，跳过压缩",
                    content.length / 1024, aliyunOSSProperties.getCompressThreshold() / 1024);
            return content;
        }

        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(content));
            if (originalImage == null) {
                log.warn("无法解析图片，跳过压缩: {}", originalFilename);
                return content;
            }

            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            int maxWidth = aliyunOSSProperties.getMaxWidth();

            BufferedImage compressedImage = originalImage;
            if (originalWidth > maxWidth) {
                int newHeight = (int) ((double) maxWidth / originalWidth * originalHeight);
                compressedImage = new BufferedImage(maxWidth, newHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = compressedImage.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.drawImage(originalImage, 0, 0, maxWidth, newHeight, null);
                g2d.dispose();
                log.info("图片缩放: {}x{} -> {}x{}", originalWidth, originalHeight, maxWidth, newHeight);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            float quality = aliyunOSSProperties.getCompressQuality();
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) {
                log.warn("未找到 JPEG ImageWriter，跳过压缩");
                return content;
            }
            ImageWriter writer = writers.next();
            try (ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream)) {
                writer.setOutput(ios);
                ImageWriteParam param = writer.getDefaultWriteParam();
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
                writer.write(null, new IIOImage(compressedImage, null, null), param);
            } finally {
                writer.dispose();
            }

            byte[] compressedBytes = outputStream.toByteArray();
            log.info("图片压缩完成: {}KB -> {}KB (压缩率 {:.1f}%)",
                    content.length / 1024, compressedBytes.length / 1024,
                    (1 - (double) compressedBytes.length / content.length) * 100);
            return compressedBytes;

        } catch (IOException e) {
            log.error("图片压缩失败，使用原图上传: {}", e.getMessage());
            return content;
        }
    }
}
