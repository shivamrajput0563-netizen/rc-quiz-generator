package com.rcpractice.rcquizgenerator.service;

import org.springframework.stereotype.Component;

@Component
public class PromptFactory {

    public String buildPrompt(String examType, String articleText) {
        String examGuidance = getExamGuidance(examType);

        return """
            You are an expert Reading Comprehension question generator for %s aspirants.
            %s

            Given an article, generate 4 multiple-choice reading comprehension questions.
            Respond ONLY with valid JSON in this exact shape, no other text:
            {
              "passageSummary": "string",
              "questions": [
                {
                  "questionText": "string",
                  "type": "INFERENCE|CENTRAL_IDEA|TONE|FACTUAL",
                  "options": ["a", "b", "c", "d"],
                  "correctOptionIndex": 0,
                  "explanation": "string"
                }
              ]
            }

            Article:
            """.formatted(examType, examGuidance) + articleText;
    }

    private String getExamGuidance(String examType) {
        return switch (examType.toUpperCase()) {
            case "CAT" -> "Focus heavily on inference, central idea, tone, and logical flaw questions. " +
                    "Distractors should be subtle — plausible but wrong due to being too broad, too narrow, " +
                    "distorted, or out of scope. Avoid straightforward factual recall questions.";
            case "GMAT" -> "Focus on inference, argument structure, and author's tone. " +
                    "Questions should test critical reasoning, not just comprehension.";
            case "SBI_PO", "IBPS_PO" -> "Focus on direct factual questions, main idea summary, and " +
                    "vocabulary-in-context questions. Keep language moderately challenging but avoid " +
                    "overly subtle inference-based distractors — this exam favors clarity over ambiguity.";
            case "UPSC" -> "Focus on comprehension, inference, and the author's central argument. " +
                    "Questions should test deep understanding of nuanced positions in the passage.";
            default -> "Generate a balanced mix of inference, factual, and central idea questions " +
                    "at a moderate difficulty level.";
        };
    }
}