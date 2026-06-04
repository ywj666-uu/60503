package com.opera.teaching.repository;

import com.opera.teaching.model.entity.SegmentAnnotation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SegmentAnnotationRepository extends JpaRepository<SegmentAnnotation, Long> {
    List<SegmentAnnotation> findBySegmentIdOrderByStartTimeAsc(Long segmentId);
}
