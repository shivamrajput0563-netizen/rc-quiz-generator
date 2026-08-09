package com.rcpractice.rcquizgenerator.dto;

import java.util.List;

public class GeminiResponse {
    private List<Candidate> candidates;

    public List<Candidate> getCandidates() { return candidates; }
    public void setCandidates(List<Candidate> candidates) { this.candidates = candidates; }

    public static class Candidate {
        private ContentBlock content;

        public ContentBlock getContent() { return content; }
        public void setContent(ContentBlock content) { this.content = content; }
    }

    public static class ContentBlock {
        private List<PartBlock> parts;

        public List<PartBlock> getParts() { return parts; }
        public void setParts(List<PartBlock> parts) { this.parts = parts; }
    }

    public static class PartBlock {
        private String text;

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}