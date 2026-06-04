package com.opera.teaching.repository;

import com.opera.teaching.model.entity.Course;
import com.opera.teaching.model.enums.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByTeacherId(Long teacherId);
    List<Course> findByStatus(CourseStatus status);
    List<Course> findByTeacherIdAndStatus(Long teacherId, CourseStatus status);
}
