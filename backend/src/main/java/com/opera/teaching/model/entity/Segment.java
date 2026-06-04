package com.opera.teaching.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "segment")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Segment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(name = "sequence_num", nullable = false)
    private Integer sequenceNum;

    @Column(name = "audio_oss_key", nullable = false, length = 512)
    private String audioOssKey;

    @Column(name = "audio_duration", precision = 8, scale = 2)
    private BigDecimal audioDuration;

    @Column(name = "ban_shi", length = 100)
    private String banShi;

    @Column(name = "diao_men", length = 50)
    private String diaoMen;

    private Integer difficulty;

    @Column(name = "difficulty_tips", columnDefinition = "TEXT")
    private String difficultyTips;

    @Column(columnDefinition = "TEXT")
    private String lyrics;

    private Integer bpm;

    @Column(name = "time_signature", length = 10)
    private String timeSignature = "4/4";

    @Column(name = "reference_pitch", columnDefinition = "JSON")
    private String referencePitch;

    @OneToMany(mappedBy = "segment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SegmentAnnotation> annotations;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
