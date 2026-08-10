package com.rcpractice.rcquizgenerator.controller;

import com.rcpractice.rcquizgenerator.entity.Passage;
import com.rcpractice.rcquizgenerator.service.GeminiApiService;
import com.rcpractice.rcquizgenerator.service.OcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    @Autowired
    private GeminiApiService geminiApiService;

    @Autowired
    private OcrService ocrService;

    @PostMapping("/generate")
    public Passage generateQuiz(@RequestParam String examType, @RequestBody String articleText) throws Exception {
        return geminiApiService.generateQuiz(examType, articleText);
    }

    @PostMapping("/generate-from-image")
    public Passage generateQuizFromImage(
            @RequestParam String examType,
            @RequestParam("image") MultipartFile imageFile) throws Exception {

        String extractedText = ocrService.extractText(imageFile);
        return geminiApiService.generateQuiz(examType, extractedText);
    }
}