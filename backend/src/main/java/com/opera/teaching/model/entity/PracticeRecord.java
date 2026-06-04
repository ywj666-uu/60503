package com.opera.teaching.model.entity;

import com.opera.teaching.model.enums.PracticeStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "practice_record")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PracticeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id", nullable = false)
    private Segment segment;

    @Column(name = "audio_oss_key", nullable = false, length = 512)
    private String audioOssKey;

    @Column(name = "audio_duration", precision = 8, scale = 2)
    private BigDecimal audioDuration;

    @Column(name = "playback_speed", precision = 3, scale = 2)
    private BigDecimal playbackSpeed = new BigDecimal("1.00");

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PracticeStatus status = PracticeStatus.UPLOADED;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
