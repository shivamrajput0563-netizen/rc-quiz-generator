package com.rcpractice.rcquizgenerator.controller;

import com.rcpractice.rcquizgenerator.entity.Passage;
import com.rcpractice.rcquizgenerator.service.GeminiApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.rcpractice.rcquizgenerator.repository.PassageRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private GeminiApiService geminiApiService;

    @Value("${admin.secret.key}")
    private String adminSecretKey;

    @PostMapping("/daily-rc")
    public ResponseEntity<?> uploadDailyRc(
            @RequestParam String examType,
            @RequestBody String articleText,
            @RequestHeader("X-Admin-Key") String providedKey) throws Exception {

        if (!adminSecretKey.equals(providedKey)) {
            return ResponseEntity.status(403).body("Invalid admin key.");
        }

        Passage passage = geminiApiService.generateDailyRc(examType, articleText);
        return ResponseEntity.ok(passage);
    }

    @Autowired
    private PassageRepository passageRepository;

    @DeleteMapping("/daily-rc/{passageId}")
    public ResponseEntity<?> deleteDailyRc(
            @PathVariable Long passageId,
            @RequestHeader("X-Admin-Key") String providedKey) {

        if (!adminSecretKey.equals(providedKey)) {
            return ResponseEntity.status(403).body("Invalid admin key.");
        }

        passageRepository.deleteById(passageId);
        return ResponseEntity.ok("Deleted passage " + passageId);
    }
}