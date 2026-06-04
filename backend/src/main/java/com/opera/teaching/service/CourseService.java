package com.opera.teaching.service;

import com.opera.teaching.exception.BusinessException;
import com.opera.teaching.exception.ResourceNotFoundException;
import com.opera.teaching.model.dto.request.CourseRequest;
import com.opera.teaching.model.entity.Course;
import com.opera.teaching.model.entity.User;
import com.opera.teaching.model.enums.CourseStatus;
import com.opera.teaching.repository.CourseRepository;
import com.opera.teaching.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final SecurityUtils securityUtils;

    public List<Course> getTeacherCourses() {
        return courseRepository.findByTeacherId(securityUtils.getCurrentUserId());
    }

    public List<Course> getPublishedCourses() {
        return courseRepository.findByStatus(CourseStatus.PUBLISHED);
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("课程不存在"));
    }

    @Transactional
    public Course createCourse(CourseRequest request) {
        User teacher = securityUtils.getCurrentUser();
        Course course = Course.builder()
                .teacher(teacher)
                .title(request.getTitle())
                .operaType(request.getOperaType())
                .description(request.getDescription())
                .coverUrl(request.getCoverUrl())
                .status(CourseStatus.DRAFT)
                .build();
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long id, CourseRequest request) {
        Course course = getCourseById(id);
        verifyOwnership(course);
        course.setTitle(request.getTitle());
        course.setOperaType(request.getOperaType());
        course.setDescription(request.getDescription());
        course.setCoverUrl(request.getCoverUrl());
        return courseRepository.save(course);
    }

    @Transactional
    public Course publishCourse(Long id) {
        Course course = getCourseById(id);
        verifyOwnership(course);
        course.setStatus(CourseStatus.PUBLISHED);
        return courseRepository.save(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = getCourseById(id);
        verifyOwnership(course);
        courseRepository.delete(course);
    }

    private void verifyOwnership(Course course) {
        if (!course.getTeacher().getId().equals(securityUtils.getCurrentUserId())) {
            throw new BusinessException("无权操作此课程");
        }
    }
}
