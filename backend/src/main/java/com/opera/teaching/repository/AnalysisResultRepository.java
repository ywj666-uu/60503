package com.opera.teaching.repository;

import com.opera.teaching.model.entity.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {
    Optional<AnalysisResult> findByPracticeRecordId(Long practiceRecordId);
}
