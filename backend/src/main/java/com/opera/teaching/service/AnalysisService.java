package com.opera.teaching.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opera.teaching.model.entity.AnalysisResult;
import com.opera.teaching.model.entity.PracticeRecord;
import com.opera.teaching.model.entity.Segment;
import com.opera.teaching.model.enums.PracticeStatus;
import com.opera.teaching.repository.AnalysisResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final AnalysisResultRepository analysisResultRepository;
    private final PracticeService practiceService;
    private final OssStorageService ossStorageService;
    private final PitchServiceClient pitchServiceClient;
    private final ObjectMapper objectMapper;

    public Optional<AnalysisResult> getAnalysisResult(Long practiceRecordId) {
        return analysisResultRepository.findByPracticeRecordId(practiceRecordId);
    }

    @Async
    @Transactional
    public void triggerAnalysis(Long practiceRecordId) {
        PracticeRecord record = practiceService.getPracticeById(practiceRecordId);
        Segment segment = record.getSegment();

        practiceService.updateStatus(practiceRecordId, PracticeStatus.ANALYZING);

        try {
            String teacherAudioUrl = ossStorageService.getSignedDownloadUrl(segment.getAudioOssKey());
            String studentAudioUrl = ossStorageService.getSignedDownloadUrl(record.getAudioOssKey());

            // 传入教师标注的调门和板式
            String diaoMen = segment.getDiaoMen();
            String banShi = segment.getBanShi();
            Map<String, Object> result = pitchServiceClient.analyzePitch(teacherAudioUrl, studentAudioUrl, diaoMen, banShi);

            AnalysisResult analysisResult = AnalysisResult.builder()
                    .practiceRecord(record)
                    .overallScore(new BigDecimal(result.get("overall_score").toString()))
                    .pitchCurveStudent(toJson(result.get("student_pitch_shifted")))
                    .pitchCurveTeacher(toJson(result.get("teacher_pitch")))
                    .errorSegments(toJson(result.get("error_segments")))
                    .summary(generateSummary(result))
                    .build();

            analysisResultRepository.save(analysisResult);
            practiceService.updateStatus(practiceRecordId, PracticeStatus.COMPLETED);

        } catch (Exception e) {
            log.error("分析失败: practiceRecordId={}", practiceRecordId, e);
            practiceService.updateStatus(practiceRecordId, PracticeStatus.FAILED);
        }
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String generateSummary(Map<String, Object> result) {
        Object score = result.get("overall_score");
        Object stats = result.get("statistics");
        if (score == null) return "分析完成";

        double scoreVal = Double.parseDouble(score.toString());
        if (scoreVal >= 90) return "优秀！音高准确度很高，继续保持。";
        if (scoreVal >= 75) return "良好。整体音准不错，部分段落可进一步精进。";
        if (scoreVal >= 60) return "及格。建议放慢速度反复练习标记的错误段落。";
        return "需要加强练习。建议使用慢速模式逐句跟唱。";
    }
}
