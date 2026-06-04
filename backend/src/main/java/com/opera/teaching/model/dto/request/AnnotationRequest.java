package com.opera.teaching.model.dto.request;

import com.opera.teaching.model.enums.AnnotationType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AnnotationRequest {
    @NotNull(message = "开始时间不能为空")
    private BigDecimal startTime;

    @NotNull(message = "结束时间不能为空")
    private BigDecimal endTime;

    private String label;
    private AnnotationType type;
    private String description;
}
