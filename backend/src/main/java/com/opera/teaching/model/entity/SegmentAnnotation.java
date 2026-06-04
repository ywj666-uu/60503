package com.opera.teaching.model.entity;

import com.opera.teaching.model.enums.AnnotationType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "segment_annotation")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class SegmentAnnotation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "segment_id", nullable = false)
    private Segment segment;

    @Column(name = "start_time", nullable = false, precision = 8, scale = 3)
    private BigDecimal startTime;

    @Column(name = "end_time", nullable = false, precision = 8, scale = 3)
    private BigDecimal endTime;

    @Column(length = 200)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnnotationType type = AnnotationType.OTHER;

    @Column(columnDefinition = "TEXT")
    private String description;
}
