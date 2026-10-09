package com.jobportal.dto;

import com.jobportal.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class ApplicationDtos {

    private ApplicationDtos() {}

    public static class CreateApplicationRequest {

        @Size(max = 2000, message = "Cover letter must be at most 2000 characters")
        private String coverLetter;

        public String getCoverLetter() { return coverLetter; }
        public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
    }

    public static class UpdateApplicationStatusRequest {

        @NotNull(message = "Status is required")
        private ApplicationStatus status;

        public ApplicationStatus getStatus() { return status; }
        public void setStatus(ApplicationStatus status) { this.status = status; }
    }

    public static class ApplicationResponse {

        private Long id;
        private Long jobId;
        private String jobTitle;
        private Long candidateId;
        private String candidateName;
        private String coverLetter;
        private ApplicationStatus status;
        private LocalDateTime appliedAt;
        private LocalDateTime updatedAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getJobId() { return jobId; }
        public void setJobId(Long jobId) { this.jobId = jobId; }
        public String getJobTitle() { return jobTitle; }
        public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
        public Long getCandidateId() { return candidateId; }
        public void setCandidateId(Long candidateId) { this.candidateId = candidateId; }
        public String getCandidateName() { return candidateName; }
        public void setCandidateName(String candidateName) { this.candidateName = candidateName; }
        public String getCoverLetter() { return coverLetter; }
        public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
        public ApplicationStatus getStatus() { return status; }
        public void setStatus(ApplicationStatus status) { this.status = status; }
        public LocalDateTime getAppliedAt() { return appliedAt; }
        public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
}
