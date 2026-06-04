package com.opera.teaching.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_result")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AnalysisResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "practice_record_id", nullable = false, unique = true)
    private PracticeRecord practiceRecord;

    @Column(name = "overall_score", precision = 5, scale = 2)
    private BigDecimal overallScore;

    @Column(name = "pitch_curve_student", columnDefinition = "JSON")
    private String pitchCurveStudent;

    @Column(name = "pitch_curve_teacher", columnDefinition = "JSON")
    private String pitchCurveTeacher;

    @Column(name = "error_segments", columnDefinition = "JSON")
    private String errorSegments;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "analyzed_at")
    private LocalDateTime analyzedAt;

    @PrePersist
    protected void onCreate() {
        analyzedAt = LocalDateTime.now();
    }
}
