package com.opera.teaching.controller;

import com.opera.teaching.model.dto.response.OssUploadCredentials;
import com.opera.teaching.service.OssStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/oss")
@RequiredArgsConstructor
public class OssController {

    private final OssStorageService ossStorageService;

    @PostMapping("/upload-credentials")
    public ResponseEntity<OssUploadCredentials> getUploadCredentials(
            @RequestBody Map<String, String> request) {
        String directory = request.get("directory");
        String filename = request.get("filename");
        OssUploadCredentials credentials = ossStorageService.generateUploadCredentials(directory, filename);
        return ResponseEntity.ok(credentials);
    }
}
