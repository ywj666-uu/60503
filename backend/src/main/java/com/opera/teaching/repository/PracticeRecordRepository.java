package com.opera.teaching.repository;

import com.opera.teaching.model.entity.PracticeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PracticeRecordRepository extends JpaRepository<PracticeRecord, Long> {
    List<PracticeRecord> findByStudentIdAndSegmentIdOrderByCreatedAtDesc(Long studentId, Long segmentId);
    List<PracticeRecord> findByStudentIdOrderByCreatedAtDesc(Long studentId);
}
