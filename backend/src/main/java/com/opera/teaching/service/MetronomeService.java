package com.opera.teaching.service;

import com.opera.teaching.model.entity.MetronomePreset;
import com.opera.teaching.model.entity.User;
import com.opera.teaching.repository.MetronomePresetRepository;
import com.opera.teaching.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MetronomeService {

    private final MetronomePresetRepository presetRepository;
    private final SecurityUtils securityUtils;

    public List<MetronomePreset> getUserPresets() {
        return presetRepository.findByUserIdOrderByCreatedAtDesc(securityUtils.getCurrentUserId());
    }

    @Transactional
    public MetronomePreset savePreset(String name, int bpm, String timeSignature, String accentPattern) {
        User user = securityUtils.getCurrentUser();
        MetronomePreset preset = MetronomePreset.builder()
                .user(user)
                .name(name)
                .bpm(bpm)
                .timeSignature(timeSignature)
                .accentPattern(accentPattern)
                .build();
        return presetRepository.save(preset);
    }

    @Transactional
    public void deletePreset(Long id) {
        presetRepository.deleteById(id);
    }
}
