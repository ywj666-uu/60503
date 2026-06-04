package com.opera.teaching.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CourseRequest {
    @NotBlank(message = "课程标题不能为空")
    private String title;
    private String operaType;
    private String description;
    private String coverUrl;
}
