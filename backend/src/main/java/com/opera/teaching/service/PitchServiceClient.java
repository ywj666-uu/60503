package com.opera.teaching.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PitchServiceClient {

    private final RestTemplate restTemplate;

    @Value("${app.pitch-service.base-url}")
    private String pitchServiceBaseUrl;

    public Map<String, Object> analyzePitch(String teacherAudioUrl, String studentAudioUrl, String diaoMen, String banShi) {
        String url = pitchServiceBaseUrl + "/api/v1/analyze";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("teacher_audio_url", teacherAudioUrl);
        requestBody.put("student_audio_url", studentAudioUrl);
        requestBody.put("diao_men", diaoMen);
        requestBody.put("ban_shi", banShi);
        requestBody.put("sample_rate", 16000);
        requestBody.put("hop_length", 160);
        requestBody.put("confidence_threshold", 0.6);
        requestBody.put("error_window_seconds", 3.0);
        requestBody.put("error_threshold_cents", 50.0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        return response.getBody();
    }

    public Map<String, Object> estimateBpm(String audioUrl) {
        String url = pitchServiceBaseUrl + "/api/v1/estimate-bpm";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("audio_url", audioUrl);
        requestBody.put("sample_rate", 16000);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        return response.getBody();
    }

    public Map<String, Object> extractPitch(String audioUrl) {
        String url = pitchServiceBaseUrl + "/api/v1/extract-pitch";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("audio_url", audioUrl);
        requestBody.put("sample_rate", 16000);
        requestBody.put("hop_length", 160);
        requestBody.put("confidence_threshold", 0.6);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        return response.getBody();
    }
}
