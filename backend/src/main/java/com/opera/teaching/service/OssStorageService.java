package com.opera.teaching.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.opera.teaching.model.dto.response.OssUploadCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OssStorageService {

    private final OSS ossClient;

    @Value("${app.oss.bucket-name}")
    private String bucketName;

    @Value("${app.oss.endpoint}")
    private String endpoint;

    @Value("${app.oss.url-expiration-minutes}")
    private int urlExpirationMinutes;

    public OssUploadCredentials generateUploadCredentials(String directory, String originalFilename) {
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectKey = directory + "/" + UUID.randomUUID() + extension;

        Date expiration = new Date(System.currentTimeMillis() + urlExpirationMinutes * 60 * 1000L);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectKey);
        request.setExpiration(expiration);
        request.setMethod(com.aliyun.oss.HttpMethod.PUT);
        request.setContentType(getContentType(extension));

        URL signedUrl = ossClient.generatePresignedUrl(request);

        return OssUploadCredentials.builder()
                .uploadUrl(signedUrl.toString())
                .objectKey(objectKey)
                .expiration(expiration.getTime())
                .build();
    }

    public String getSignedDownloadUrl(String objectKey) {
        Date expiration = new Date(System.currentTimeMillis() + urlExpirationMinutes * 60 * 1000L);
        URL url = ossClient.generatePresignedUrl(bucketName, objectKey, expiration);
        return url.toString();
    }

    public void deleteObject(String objectKey) {
        ossClient.deleteObject(bucketName, objectKey);
    }

    private String getContentType(String extension) {
        return switch (extension.toLowerCase()) {
            case ".wav" -> "audio/wav";
            case ".mp3" -> "audio/mpeg";
            case ".webm" -> "audio/webm";
            case ".ogg" -> "audio/ogg";
            case ".flac" -> "audio/flac";
            default -> "application/octet-stream";
        };
    }
}
