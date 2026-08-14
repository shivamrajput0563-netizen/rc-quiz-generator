package com.rcpractice.rcquizgenerator.controller;
import org.springframework.http.ResponseEntity;

import com.rcpractice.rcquizgenerator.entity.Passage;
import com.rcpractice.rcquizgenerator.repository.PassageRepository;
import com.rcpractice.rcquizgenerator.service.GeminiApiService;
import com.rcpractice.rcquizgenerator.service.OcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import  com.rcpractice.rcquizgenerator.service.ArticleFetcherService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;


@RestController
@RequestMapping("/api/quiz")
public class QuizController {



    @Autowired
    private GeminiApiService geminiApiService;

    @Autowired
    private OcrService ocrService;

    @Autowired
    private PassageRepository passageRepository;

    @Autowired
    private ArticleFetcherService articleFetcherService;

    @PostMapping("/generate-from-url")
    public ResponseEntity<?> generateQuizFromUrl(
            @RequestParam String examType,
            @RequestBody String url,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) {
        try {
            String extractedText = articleFetcherService.extractArticleText(url.trim());
            Passage passage = geminiApiService.generateQuiz(examType, extractedText, sessionId);
            return ResponseEntity.ok(passage);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/generate")
    public Passage generateQuiz(
            @RequestParam String examType,
            @RequestBody String articleText,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) throws Exception {
        return geminiApiService.generateQuiz(examType, articleText, sessionId);
    }

    @PostMapping("/generate-from-image")
    public Passage generateQuizFromImage(
            @RequestParam String examType,
            @RequestParam("image") MultipartFile imageFile,
            @RequestHeader(value = "X-Session-Id", required = false) String sessionId) throws Exception {

        String extractedText = ocrService.extractText(imageFile);
        return geminiApiService.generateQuiz(examType, extractedText, sessionId);
    }

    @GetMapping("/stats/today")
    public Map<String, Long> getTodayStats() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayCount = passageRepository.countGeneratedSince(startOfDay);
        long totalCount = passageRepository.count(); // built-in method, no new query needed
        return Map.of("quizzesToday", todayCount, "quizzesTotal", totalCount);
    }
    @GetMapping("/daily")
    public Passage getTodaysDailyRc() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return passageRepository.findTodaysDailyRc(startOfDay).orElse(null);
    }

    @GetMapping("/daily/archive")
    public List<Passage> getDailyRcArchive() {
        return passageRepository.findAllDailyRcHistory();
    }



}