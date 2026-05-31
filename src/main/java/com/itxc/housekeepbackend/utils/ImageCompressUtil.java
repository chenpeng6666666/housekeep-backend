// 路径: src/main/java/com/itxc/housekeepbackend/utils/ImageCompressUtil.java
package com.itxc.housekeepbackend.utils;

import lombok.extern.slf4j.Slf4j;

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
import java.util.Iterator;

@Slf4j
public class ImageCompressUtil {

    public static byte[] compress(byte[] content, String originalFilename, StorageProperties properties) {
        if (!properties.isEnableCompress()) {
            return content;
        }
        if (content.length <= properties.getCompressThreshold()) {
            log.info("图片大小 {}KB 未超过阈值 {}KB，跳过压缩",
                    content.length / 1024, properties.getCompressThreshold() / 1024);
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
            int maxWidth = properties.getMaxWidth();

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
            float quality = properties.getCompressQuality();
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
            log.info("图片压缩完成: {}KB -> {}KB", content.length / 1024, compressedBytes.length / 1024);
            return compressedBytes;
        } catch (IOException e) {
            log.error("图片压缩失败，使用原图: {}", e.getMessage());
            return content;
        }
    }
}
