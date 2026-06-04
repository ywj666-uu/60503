package com.opera.teaching.controller;

import com.opera.teaching.model.entity.AnalysisResult;
import com.opera.teaching.model.entity.PracticeRecord;
import com.opera.teaching.service.AnalysisService;
import com.opera.teaching.service.OssStorageService;
import com.opera.teaching.service.PracticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PracticeController {

    private final PracticeService practiceService;
    private final AnalysisService analysisService;
    private final OssStorageService ossStorageService;

    @PostMapping("/segments/{segmentId}/practice")
    public ResponseEntity<PracticeRecord> submitPractice(
            @PathVariable Long segmentId,
            @RequestBody Map<String, Object> body) {
        String audioOssKey = (String) body.get("audioOssKey");
        BigDecimal duration = body.get("audioDuration") != null
                ? new BigDecimal(body.get("audioDuration").toString()) : null;
        BigDecimal speed = body.get("playbackSpeed") != null
                ? new BigDecimal(body.get("playbackSpeed").toString()) : null;

        PracticeRecord record = practiceService.submitPractice(segmentId, audioOssKey, duration, speed);
        return ResponseEntity.ok(record);
    }

    @GetMapping("/segments/{segmentId}/practice")
    public ResponseEntity<List<PracticeRecord>> getPracticeHistory(@PathVariable Long segmentId) {
        return ResponseEntity.ok(practiceService.getStudentPracticeHistory(segmentId));
    }

    @PostMapping("/practice/{id}/analyze")
    public ResponseEntity<Map<String, String>> triggerAnalysis(@PathVariable Long id) {
        analysisService.triggerAnalysis(id);
        return ResponseEntity.ok(Map.of("message", "分析已开始", "status", "ANALYZING"));
    }

    @GetMapping("/practice/{id}/analysis")
    public ResponseEntity<AnalysisResult> getAnalysis(@PathVariable Long id) {
        return analysisService.getAnalysisResult(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/practice/{id}")
    public ResponseEntity<Map<String, Object>> getPracticeDetail(@PathVariable Long id) {
        PracticeRecord record = practiceService.getPracticeById(id);
        String audioUrl = ossStorageService.getSignedDownloadUrl(record.getAudioOssKey());
        return ResponseEntity.ok(Map.of("record", record, "audioUrl", audioUrl));
    }
}
