package com.opera.teaching.model.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "metronome_preset")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class MetronomePreset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 100)
    private String name;

    @Column(nullable = false)
    private Integer bpm;

    @Column(name = "time_signature", nullable = false, length = 10)
    private String timeSignature = "4/4";

    @Column(name = "accent_pattern", length = 50)
    private String accentPattern;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
