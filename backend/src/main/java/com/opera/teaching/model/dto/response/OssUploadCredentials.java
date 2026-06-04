package com.opera.teaching.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OssUploadCredentials {
    private String uploadUrl;
    private String objectKey;
    private Long expiration;
}
