package com.rcpractice.rcquizgenerator.dto;

import java.util.List;

public class QuizQuestion {
    private String questionText;
    private String type;
    private List<String> options;
    private int correctOptionIndex;
    private String explanation;

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public int getCorrectOptionIndex() { return correctOptionIndex; }
    public void setCorrectOptionIndex(int correctOptionIndex) { this.correctOptionIndex = correctOptionIndex; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}