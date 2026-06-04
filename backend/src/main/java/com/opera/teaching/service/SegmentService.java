package com.opera.teaching.service;

import com.opera.teaching.exception.ResourceNotFoundException;
import com.opera.teaching.model.dto.request.AnnotationRequest;
import com.opera.teaching.model.dto.request.SegmentUploadRequest;
import com.opera.teaching.model.entity.Course;
import com.opera.teaching.model.entity.Segment;
import com.opera.teaching.model.entity.SegmentAnnotation;
import com.opera.teaching.model.enums.AnnotationType;
import com.opera.teaching.repository.SegmentAnnotationRepository;
import com.opera.teaching.repository.SegmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SegmentService {

    private final SegmentRepository segmentRepository;
    private final SegmentAnnotationRepository annotationRepository;
    private final CourseService courseService;

    public List<Segment> getSegmentsByCourse(Long courseId) {
        return segmentRepository.findByCourseIdOrderBySequenceNumAsc(courseId);
    }

    public Segment getSegmentById(Long id) {
        return segmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("唱段不存在"));
    }

    @Transactional
    public Segment createSegment(Long courseId, SegmentUploadRequest request) {
        Course course = courseService.getCourseById(courseId);
        int nextSeq = segmentRepository.countByCourseId(courseId) + 1;

        Segment segment = Segment.builder()
                .course(course)
                .title(request.getTitle())
                .sequenceNum(nextSeq)
                .audioOssKey(request.getAudioOssKey())
                .audioDuration(request.getAudioDuration())
                .banShi(request.getBanShi())
                .diaoMen(request.getDiaoMen())
                .difficulty(request.getDifficulty())
                .difficultyTips(request.getDifficultyTips())
                .lyrics(request.getLyrics())
                .bpm(request.getBpm())
                .timeSignature(request.getTimeSignature() != null ? request.getTimeSignature() : "4/4")
                .build();

        return segmentRepository.save(segment);
    }

    @Transactional
    public Segment updateSegment(Long id, SegmentUploadRequest request) {
        Segment segment = getSegmentById(id);
        segment.setTitle(request.getTitle());
        segment.setBanShi(request.getBanShi());
        segment.setDiaoMen(request.getDiaoMen());
        segment.setDifficulty(request.getDifficulty());
        segment.setDifficultyTips(request.getDifficultyTips());
        segment.setLyrics(request.getLyrics());
        segment.setBpm(request.getBpm());
        if (request.getTimeSignature() != null) {
            segment.setTimeSignature(request.getTimeSignature());
        }
        return segmentRepository.save(segment);
    }

    @Transactional
    public void deleteSegment(Long id) {
        Segment segment = getSegmentById(id);
        segmentRepository.delete(segment);
    }

    @Transactional
    public SegmentAnnotation addAnnotation(Long segmentId, AnnotationRequest request) {
        Segment segment = getSegmentById(segmentId);
        SegmentAnnotation annotation = SegmentAnnotation.builder()
                .segment(segment)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .label(request.getLabel())
                .type(request.getType() != null ? request.getType() : AnnotationType.OTHER)
                .description(request.getDescription())
                .build();
        return annotationRepository.save(annotation);
    }

    public List<SegmentAnnotation> getAnnotations(Long segmentId) {
        return annotationRepository.findBySegmentIdOrderByStartTimeAsc(segmentId);
    }

    @Transactional
    public void deleteAnnotation(Long annotationId) {
        annotationRepository.deleteById(annotationId);
    }
}
