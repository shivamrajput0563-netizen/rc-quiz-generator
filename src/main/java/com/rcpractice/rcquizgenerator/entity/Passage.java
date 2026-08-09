package com.rcpractice.rcquizgenerator.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "passages")
public class Passage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long passageId;

    @Column(columnDefinition = "TEXT")
    private String cleanedText;

    @Column(columnDefinition = "TEXT")
    private String passageSummary;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "passage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Question> questions;

    // Default constructor (required by JPA)
    public Passage() {}

    // to decide which exam is user taking
    private String examType;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getPassageId() { return passageId; }
    public void setPassageId(Long passageId) { this.passageId = passageId; }
    public String getCleanedText() { return cleanedText; }
    public void setCleanedText(String cleanedText) { this.cleanedText = cleanedText; }
    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }
    public String getPassageSummary() { return passageSummary; }
    public void setPassageSummary(String passageSummary) { this.passageSummary = passageSummary; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<Question> getQuestions() { return questions; }
    public void setQuestions(List<Question> questions) { this.questions = questions; }
}