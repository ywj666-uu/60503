package com.opera.teaching.controller;

import com.opera.teaching.model.dto.request.AnnotationRequest;
import com.opera.teaching.model.dto.request.SegmentUploadRequest;
import com.opera.teaching.model.entity.Segment;
import com.opera.teaching.model.entity.SegmentAnnotation;
import com.opera.teaching.service.OssStorageService;
import com.opera.teaching.service.PitchServiceClient;
import com.opera.teaching.service.SegmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SegmentController {

    private final SegmentService segmentService;
    private final OssStorageService ossStorageService;
    private final PitchServiceClient pitchServiceClient;

    @GetMapping("/courses/{courseId}/segments")
    public ResponseEntity<List<Segment>> listSegments(@PathVariable Long courseId) {
        return ResponseEntity.ok(segmentService.getSegmentsByCourse(courseId));
    }

    @GetMapping("/segments/{id}")
    public ResponseEntity<Map<String, Object>> getSegment(@PathVariable Long id) {
        Segment segment = segmentService.getSegmentById(id);
        String audioUrl = ossStorageService.getSignedDownloadUrl(segment.getAudioOssKey());
        List<SegmentAnnotation> annotations = segmentService.getAnnotations(id);

        Map<String, Object> response = new HashMap<>();
        response.put("segment", segment);
        response.put("audioUrl", audioUrl);
        response.put("annotations", annotations);

        // 如果教师未手动设置 BPM，从音频自动估算
        if (segment.getBpm() == null || segment.getBpm() == 0) {
            try {
                Map<String, Object> bpmResult = pitchServiceClient.estimateBpm(audioUrl);
                response.put("estimatedBpm", bpmResult);
            } catch (Exception e) {
                response.put("estimatedBpm", null);
            }
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/courses/{courseId}/segments")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Segment> createSegment(@PathVariable Long courseId,
                                                  @Valid @RequestBody SegmentUploadRequest request) {
        return ResponseEntity.ok(segmentService.createSegment(courseId, request));
    }

    @PutMapping("/segments/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Segment> updateSegment(@PathVariable Long id,
                                                  @Valid @RequestBody SegmentUploadRequest request) {
        return ResponseEntity.ok(segmentService.updateSegment(id, request));
    }

    @DeleteMapping("/segments/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteSegment(@PathVariable Long id) {
        segmentService.deleteSegment(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/segments/{id}/annotations")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SegmentAnnotation> addAnnotation(@PathVariable Long id,
                                                           @Valid @RequestBody AnnotationRequest request) {
        return ResponseEntity.ok(segmentService.addAnnotation(id, request));
    }

    @GetMapping("/segments/{id}/annotations")
    public ResponseEntity<List<SegmentAnnotation>> getAnnotations(@PathVariable Long id) {
        return ResponseEntity.ok(segmentService.getAnnotations(id));
    }

    @DeleteMapping("/annotations/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<Void> deleteAnnotation(@PathVariable Long id) {
        segmentService.deleteAnnotation(id);
        return ResponseEntity.noContent().build();
    }
}
