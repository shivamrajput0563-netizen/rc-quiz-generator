package com.rcpractice.rcquizgenerator.dto;

import java.util.List;

public class QuizResponse {
    private String passageSummary;
    private List<QuizQuestion> questions;

    public String getPassageSummary() { return passageSummary; }
    public void setPassageSummary(String passageSummary) { this.passageSummary = passageSummary; }
    public List<QuizQuestion> getQuestions() { return questions; }
    public void setQuestions(List<QuizQuestion> questions) { this.questions = questions; }
}