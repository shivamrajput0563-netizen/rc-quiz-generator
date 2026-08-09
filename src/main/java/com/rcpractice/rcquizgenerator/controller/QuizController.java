package com.rcpractice.rcquizgenerator.controller;

import com.rcpractice.rcquizgenerator.entity.Passage;
import com.rcpractice.rcquizgenerator.service.GeminiApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    @Autowired
    private GeminiApiService geminiApiService;

    @PostMapping("/generate")
    public Passage generateQuiz(@RequestParam String examType, @RequestBody String articleText) throws Exception {
        return geminiApiService.generateQuiz(examType, articleText);
    }
}