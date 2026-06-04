package com.opera.teaching.repository;

import com.opera.teaching.model.entity.MetronomePreset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MetronomePresetRepository extends JpaRepository<MetronomePreset, Long> {
    List<MetronomePreset> findByUserIdOrderByCreatedAtDesc(Long userId);
}
