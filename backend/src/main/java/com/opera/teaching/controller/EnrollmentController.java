package com.opera.teaching.controller;

import com.opera.teaching.model.entity.Enrollment;
import com.opera.teaching.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/courses/{courseId}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Enrollment> enroll(@PathVariable Long courseId) {
        return ResponseEntity.ok(enrollmentService.enroll(courseId));
    }

    @DeleteMapping("/courses/{courseId}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> unenroll(@PathVariable Long courseId) {
        enrollmentService.unenroll(courseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my/enrollments")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Enrollment>> getMyEnrollments() {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments());
    }
}
