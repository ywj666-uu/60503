package com.opera.teaching.repository;

import com.opera.teaching.model.entity.Segment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SegmentRepository extends JpaRepository<Segment, Long> {
    List<Segment> findByCourseIdOrderBySequenceNumAsc(Long courseId);
    int countByCourseId(Long courseId);
}
