package com.opera.teaching.controller;

import com.opera.teaching.model.entity.MetronomePreset;
import com.opera.teaching.service.MetronomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metronome/presets")
@RequiredArgsConstructor
public class MetronomeController {

    private final MetronomeService metronomeService;

    @GetMapping
    public ResponseEntity<List<MetronomePreset>> getPresets() {
        return ResponseEntity.ok(metronomeService.getUserPresets());
    }

    @PostMapping
    public ResponseEntity<MetronomePreset> savePreset(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        int bpm = ((Number) body.get("bpm")).intValue();
        String timeSignature = (String) body.getOrDefault("timeSignature", "4/4");
        String accentPattern = (String) body.get("accentPattern");
        return ResponseEntity.ok(metronomeService.savePreset(name, bpm, timeSignature, accentPattern));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePreset(@PathVariable Long id) {
        metronomeService.deletePreset(id);
        return ResponseEntity.noContent().build();
    }
}
