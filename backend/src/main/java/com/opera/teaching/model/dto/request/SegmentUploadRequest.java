package com.opera.teaching.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SegmentUploadRequest {
    @NotBlank(message = "唱段标题不能为空")
    private String title;

    @NotBlank(message = "音频文件Key不能为空")
    private String audioOssKey;

    private BigDecimal audioDuration;
    private String banShi;
    private String diaoMen;
    private Integer difficulty;
    private String difficultyTips;
    private String lyrics;
    private Integer bpm;
    private String timeSignature;
}
