package com.rcpractice.rcquizgenerator.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationResult {
    private boolean valid = true;
    private List<String> issues = new ArrayList<>();

    public void addIssue(String issue) {
        this.valid = false;
        this.issues.add(issue);
    }

    public boolean isValid() { return valid; }
    public List<String> getIssues() { return issues; }
}