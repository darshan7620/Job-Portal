package com.jobportal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class AiDtos {

    private AiDtos() {}

    public static class MatchRequest {

        @NotEmpty(message = "Add at least one skill")
        @Size(max = 50, message = "Please provide at most 50 skills")
        private List<
                @NotBlank(message = "Skills cannot be blank")
                @Size(max = 100, message = "Each skill must be at most 100 characters")
                        String> skills;

        @NotBlank(message = "Job description is required")
        @Size(max = 4000, message = "Job description must be at most 4000 characters")
        private String jobDescription;

        public List<String> getSkills() { return skills; }
        public void setSkills(List<String> skills) { this.skills = skills; }
        public String getJobDescription() { return jobDescription; }
        public void setJobDescription(String jobDescription) { this.jobDescription = jobDescription; }
    }

    public static class MatchResponse {

        private List<String> matchingSkills;
        private List<String> missingSkills;
        private List<String> improvementSuggestions;
        private String summary;
        private int matchScore;

        // Server-authored note so clients always see the score framed correctly,
        // regardless of what the model returns.
        private String matchScoreNote;

        public List<String> getMatchingSkills() { return matchingSkills; }
        public void setMatchingSkills(List<String> matchingSkills) { this.matchingSkills = matchingSkills; }
        public List<String> getMissingSkills() { return missingSkills; }
        public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }
        public List<String> getImprovementSuggestions() { return improvementSuggestions; }
        public void setImprovementSuggestions(List<String> improvementSuggestions) {
            this.improvementSuggestions = improvementSuggestions;
        }
        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public int getMatchScore() { return matchScore; }
        public void setMatchScore(int matchScore) { this.matchScore = matchScore; }
        public String getMatchScoreNote() { return matchScoreNote; }
        public void setMatchScoreNote(String matchScoreNote) { this.matchScoreNote = matchScoreNote; }
    }
}
