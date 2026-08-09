package com.rcpractice.rcquizgenerator.validation;

import com.rcpractice.rcquizgenerator.dto.QuizQuestion;
import com.rcpractice.rcquizgenerator.dto.QuizResponse;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class QuizValidator {

    public ValidationResult validate(QuizResponse quizResponse) {
        ValidationResult result = new ValidationResult();

        if (quizResponse.getQuestions() == null || quizResponse.getQuestions().isEmpty()) {
            result.addIssue("No questions generated.");
            return result; // nothing further to check
        }

        int qNum = 0;
        for (QuizQuestion q : quizResponse.getQuestions()) {
            qNum++;
            checkOptionCount(q, qNum, result);
            checkCorrectIndexRange(q, qNum, result);
            checkNearDuplicateOptions(q, qNum, result);
            checkLengthRatio(q, qNum, result);
            checkExplanationPresent(q, qNum, result);
        }

        return result;
    }

    private void checkOptionCount(QuizQuestion q, int qNum, ValidationResult result) {
        if (q.getOptions() == null || q.getOptions().size() != 4) {
            result.addIssue("Q" + qNum + ": expected 4 options, found " +
                    (q.getOptions() == null ? 0 : q.getOptions().size()));
        }
    }

    private void checkCorrectIndexRange(QuizQuestion q, int qNum, ValidationResult result) {
        int idx = q.getCorrectOptionIndex();
        if (idx < 0 || q.getOptions() == null || idx >= q.getOptions().size()) {
            result.addIssue("Q" + qNum + ": correctOptionIndex " + idx + " is out of range.");
        }
    }

    private void checkNearDuplicateOptions(QuizQuestion q, int qNum, ValidationResult result) {
        if (q.getOptions() == null) return;
        List<String> options = q.getOptions();
        for (int i = 0; i < options.size(); i++) {
            for (int j = i + 1; j < options.size(); j++) {
                double similarity = wordOverlapSimilarity(options.get(i), options.get(j));
                if (similarity > 0.7) {
                    result.addIssue("Q" + qNum + ": options " + (i + 1) + " and " + (j + 1) +
                            " are too similar (similarity=" + String.format("%.2f", similarity) + ")");
                }
            }
        }
    }

    // Simple word-overlap similarity: shared significant words / total unique words
    private double wordOverlapSimilarity(String a, String b) {
        Set<String> wordsA = new HashSet<>(List.of(a.toLowerCase().split("\\s+")));
        Set<String> wordsB = new HashSet<>(List.of(b.toLowerCase().split("\\s+")));

        Set<String> intersection = new HashSet<>(wordsA);
        intersection.retainAll(wordsB);

        Set<String> union = new HashSet<>(wordsA);
        union.addAll(wordsB);

        if (union.isEmpty()) return 0.0;
        return (double) intersection.size() / union.size();
    }

    private void checkLengthRatio(QuizQuestion q, int qNum, ValidationResult result) {
        if (q.getOptions() == null || q.getOptions().size() != 4) return;

        int correctIdx = q.getCorrectOptionIndex();
        if (correctIdx < 0 || correctIdx >= q.getOptions().size()) return;

        int correctLength = q.getOptions().get(correctIdx).length();
        double avgDistractorLength = q.getOptions().stream()
                .filter(opt -> !opt.equals(q.getOptions().get(correctIdx)))
                .mapToInt(String::length)
                .average()
                .orElse(0);

        if (avgDistractorLength > 0 && correctLength > avgDistractorLength * 1.5) {
            result.addIssue("Q" + qNum + ": correct option is suspiciously longer than distractors " +
                    "(possible easy-answer tell).");
        }
    }

    private void checkExplanationPresent(QuizQuestion q, int qNum, ValidationResult result) {
        if (q.getExplanation() == null || q.getExplanation().trim().length() < 10) {
            result.addIssue("Q" + qNum + ": explanation missing or too short.");
        }
    }
}