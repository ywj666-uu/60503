package com.opera.teaching.service;

import com.opera.teaching.exception.ResourceNotFoundException;
import com.opera.teaching.model.entity.PracticeRecord;
import com.opera.teaching.model.entity.Segment;
import com.opera.teaching.model.entity.User;
import com.opera.teaching.model.enums.PracticeStatus;
import com.opera.teaching.repository.PracticeRecordRepository;
import com.opera.teaching.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PracticeService {

    private final PracticeRecordRepository practiceRecordRepository;
    private final SegmentService segmentService;
    private final SecurityUtils securityUtils;

    public List<PracticeRecord> getStudentPracticeHistory(Long segmentId) {
        Long studentId = securityUtils.getCurrentUserId();
        return practiceRecordRepository.findByStudentIdAndSegmentIdOrderByCreatedAtDesc(studentId, segmentId);
    }

    public PracticeRecord getPracticeById(Long id) {
        return practiceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("练习记录不存在"));
    }

    @Transactional
    public PracticeRecord submitPractice(Long segmentId, String audioOssKey,
                                          BigDecimal audioDuration, BigDecimal playbackSpeed) {
        User student = securityUtils.getCurrentUser();
        Segment segment = segmentService.getSegmentById(segmentId);

        PracticeRecord record = PracticeRecord.builder()
                .student(student)
                .segment(segment)
                .audioOssKey(audioOssKey)
                .audioDuration(audioDuration)
                .playbackSpeed(playbackSpeed != null ? playbackSpeed : new BigDecimal("1.00"))
                .status(PracticeStatus.UPLOADED)
                .build();

        return practiceRecordRepository.save(record);
    }

    @Transactional
    public void updateStatus(Long id, PracticeStatus status) {
        PracticeRecord record = getPracticeById(id);
        record.setStatus(status);
        practiceRecordRepository.save(record);
    }
}
