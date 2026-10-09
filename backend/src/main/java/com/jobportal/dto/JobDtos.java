package com.jobportal.dto;

import com.jobportal.entity.JobStatus;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class JobDtos {

    private JobDtos() {}

    public static class CreateJobRequest {

        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must be at most 150 characters")
        private String title;

        @NotBlank(message = "Company is required")
        @Size(max = 150, message = "Company must be at most 150 characters")
        private String company;

        @Size(max = 150, message = "Location must be at most 150 characters")
        private String location;

        @NotBlank(message = "Description is required")
        private String description;

        @Size(max = 1000, message = "Required skills must be at most 1000 characters")
        private String requiredSkills;

        @Size(max = 50, message = "Employment type must be at most 50 characters")
        private String employmentType;

        private JobStatus status;

        @Min(value = 0, message = "Minimum salary cannot be negative")
        private Integer salaryMin;

        @Min(value = 0, message = "Maximum salary cannot be negative")
        private Integer salaryMax;

        @FutureOrPresent(message = "Expiry date cannot be in the past")
        private LocalDate expiresAt;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getRequiredSkills() { return requiredSkills; }
        public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
        public String getEmploymentType() { return employmentType; }
        public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
        public JobStatus getStatus() { return status; }
        public void setStatus(JobStatus status) { this.status = status; }
        public Integer getSalaryMin() { return salaryMin; }
        public void setSalaryMin(Integer salaryMin) { this.salaryMin = salaryMin; }
        public Integer getSalaryMax() { return salaryMax; }
        public void setSalaryMax(Integer salaryMax) { this.salaryMax = salaryMax; }
        public LocalDate getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDate expiresAt) { this.expiresAt = expiresAt; }
    }

    public static class UpdateJobRequest {

        @Size(max = 150, message = "Title must be at most 150 characters")
        private String title;

        @Size(max = 150, message = "Company must be at most 150 characters")
        private String company;

        @Size(max = 150, message = "Location must be at most 150 characters")
        private String location;

        private String description;

        @Size(max = 1000, message = "Required skills must be at most 1000 characters")
        private String requiredSkills;

        @Size(max = 50, message = "Employment type must be at most 50 characters")
        private String employmentType;

        private JobStatus status;

        @Min(value = 0, message = "Minimum salary cannot be negative")
        private Integer salaryMin;

        @Min(value = 0, message = "Maximum salary cannot be negative")
        private Integer salaryMax;

        @FutureOrPresent(message = "Expiry date cannot be in the past")
        private LocalDate expiresAt;

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getRequiredSkills() { return requiredSkills; }
        public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
        public String getEmploymentType() { return employmentType; }
        public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
        public JobStatus getStatus() { return status; }
        public void setStatus(JobStatus status) { this.status = status; }
        public Integer getSalaryMin() { return salaryMin; }
        public void setSalaryMin(Integer salaryMin) { this.salaryMin = salaryMin; }
        public Integer getSalaryMax() { return salaryMax; }
        public void setSalaryMax(Integer salaryMax) { this.salaryMax = salaryMax; }
        public LocalDate getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDate expiresAt) { this.expiresAt = expiresAt; }
    }

    public static class JobResponse {

        private Long id;
        private String title;
        private String company;
        private String location;
        private String description;
        private String requiredSkills;
        private String employmentType;
        private Integer salaryMin;
        private Integer salaryMax;
        private JobStatus status;
        private LocalDate expiresAt;
        private Long postedByUserId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getCompany() { return company; }
        public void setCompany(String company) { this.company = company; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getRequiredSkills() { return requiredSkills; }
        public void setRequiredSkills(String requiredSkills) { this.requiredSkills = requiredSkills; }
        public String getEmploymentType() { return employmentType; }
        public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }
        public Integer getSalaryMin() { return salaryMin; }
        public void setSalaryMin(Integer salaryMin) { this.salaryMin = salaryMin; }
        public Integer getSalaryMax() { return salaryMax; }
        public void setSalaryMax(Integer salaryMax) { this.salaryMax = salaryMax; }
        public JobStatus getStatus() { return status; }
        public void setStatus(JobStatus status) { this.status = status; }
        public LocalDate getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDate expiresAt) { this.expiresAt = expiresAt; }
        public Long getPostedByUserId() { return postedByUserId; }
        public void setPostedByUserId(Long postedByUserId) { this.postedByUserId = postedByUserId; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    }
}
