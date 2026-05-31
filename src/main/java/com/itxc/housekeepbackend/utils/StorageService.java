package com.itxc.housekeepbackend.utils;

import com.itxc.housekeepbackend.enums.ImageType;

public interface StorageService {

    String upload(byte[] content, String originalFilename, ImageType imageType) throws Exception;
}