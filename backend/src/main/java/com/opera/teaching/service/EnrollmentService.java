package com.opera.teaching.service;

import com.opera.teaching.exception.BusinessException;
import com.opera.teaching.model.entity.Enrollment;
import com.opera.teaching.model.entity.Course;
import com.opera.teaching.model.entity.User;
import com.opera.teaching.repository.EnrollmentRepository;
import com.opera.teaching.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseService courseService;
    private final SecurityUtils securityUtils;

    public List<Enrollment> getMyEnrollments() {
        return enrollmentRepository.findByStudentId(securityUtils.getCurrentUserId());
    }

    @Transactional
    public Enrollment enroll(Long courseId) {
        Long studentId = securityUtils.getCurrentUserId();
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new BusinessException("已选修此课程");
        }

        User student = securityUtils.getCurrentUser();
        Course course = courseService.getCourseById(courseId);

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .build();
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void unenroll(Long courseId) {
        Long studentId = securityUtils.getCurrentUserId();
        Enrollment enrollment = enrollmentRepository.findByStudentIdAndCourseId(studentId, courseId)
                .orElseThrow(() -> new BusinessException("未选修此课程"));
        enrollmentRepository.delete(enrollment);
    }
}
