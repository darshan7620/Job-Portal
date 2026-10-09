package com.jobportal.dto;

import com.jobportal.entity.ApplicationStatus;
import com.jobportal.entity.JobStatus;

import java.util.List;

public final class DashboardDtos {

    private DashboardDtos() {}

    // A status plus its aggregated count. Always emitted for every enum value,
    // including zeros, so the frontend can render a complete breakdown.
    public static class ApplicationStatusCount {
        private ApplicationStatus status;
        private long count;

        public ApplicationStatusCount() {}

        public ApplicationStatusCount(ApplicationStatus status, long count) {
            this.status = status;
            this.count = count;
        }

        public ApplicationStatus getStatus() { return status; }
        public void setStatus(ApplicationStatus status) { this.status = status; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    public static class JobStatusCount {
        private JobStatus status;
        private long count;

        public JobStatusCount() {}

        public JobStatusCount(JobStatus status, long count) {
            this.status = status;
            this.count = count;
        }

        public JobStatus getStatus() { return status; }
        public void setStatus(JobStatus status) { this.status = status; }
        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    public static class JobApplicantCount {
        private Long jobId;
        private String jobTitle;
        private JobStatus status;
        private long applicantCount;

        public Long getJobId() { return jobId; }
        public void setJobId(Long jobId) { this.jobId = jobId; }
        public String getJobTitle() { return jobTitle; }
        public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }
        public JobStatus getStatus() { return status; }
        public void setStatus(JobStatus status) { this.status = status; }
        public long getApplicantCount() { return applicantCount; }
        public void setApplicantCount(long applicantCount) { this.applicantCount = applicantCount; }
    }

    public static class CandidateDashboardResponse {
        private long totalApplications;
        private long underReviewApplications;
        private long shortlistedApplications;
        private long rejectedApplications;
        private long offers;
        private List<ApplicationStatusCount> applicationsByStatus;
        private List<ApplicationDtos.ApplicationResponse> recentApplications;
        private List<ApplicationDtos.ApplicationResponse> recentlyUpdatedApplications;

        public long getTotalApplications() { return totalApplications; }
        public void setTotalApplications(long totalApplications) { this.totalApplications = totalApplications; }
        public long getUnderReviewApplications() { return underReviewApplications; }
        public void setUnderReviewApplications(long underReviewApplications) {
            this.underReviewApplications = underReviewApplications;
        }
        public long getShortlistedApplications() { return shortlistedApplications; }
        public void setShortlistedApplications(long shortlistedApplications) {
            this.shortlistedApplications = shortlistedApplications;
        }
        public long getRejectedApplications() { return rejectedApplications; }
        public void setRejectedApplications(long rejectedApplications) {
            this.rejectedApplications = rejectedApplications;
        }
        public long getOffers() { return offers; }
        public void setOffers(long offers) { this.offers = offers; }
        public List<ApplicationStatusCount> getApplicationsByStatus() { return applicationsByStatus; }
        public void setApplicationsByStatus(List<ApplicationStatusCount> applicationsByStatus) {
            this.applicationsByStatus = applicationsByStatus;
        }
        public List<ApplicationDtos.ApplicationResponse> getRecentApplications() { return recentApplications; }
        public void setRecentApplications(List<ApplicationDtos.ApplicationResponse> recentApplications) {
            this.recentApplications = recentApplications;
        }
        public List<ApplicationDtos.ApplicationResponse> getRecentlyUpdatedApplications() {
            return recentlyUpdatedApplications;
        }
        public void setRecentlyUpdatedApplications(
                List<ApplicationDtos.ApplicationResponse> recentlyUpdatedApplications) {
            this.recentlyUpdatedApplications = recentlyUpdatedApplications;
        }
    }

    public static class HrDashboardResponse {
        private long totalJobs;
        private long draftJobs;
        private long publishedJobs;
        private long closedJobs;
        private long totalApplicants;
        private List<JobStatusCount> jobsByStatus;
        private List<ApplicationStatusCount> applicationsByStatus;
        private List<ApplicationDtos.ApplicationResponse> recentApplications;
        private List<JobApplicantCount> applicantsPerJob;
        private List<JobApplicantCount> topJobsByApplicants;

        public long getTotalJobs() { return totalJobs; }
        public void setTotalJobs(long totalJobs) { this.totalJobs = totalJobs; }
        public long getDraftJobs() { return draftJobs; }
        public void setDraftJobs(long draftJobs) { this.draftJobs = draftJobs; }
        public long getPublishedJobs() { return publishedJobs; }
        public void setPublishedJobs(long publishedJobs) { this.publishedJobs = publishedJobs; }
        public long getClosedJobs() { return closedJobs; }
        public void setClosedJobs(long closedJobs) { this.closedJobs = closedJobs; }
        public long getTotalApplicants() { return totalApplicants; }
        public void setTotalApplicants(long totalApplicants) { this.totalApplicants = totalApplicants; }
        public List<JobStatusCount> getJobsByStatus() { return jobsByStatus; }
        public void setJobsByStatus(List<JobStatusCount> jobsByStatus) { this.jobsByStatus = jobsByStatus; }
        public List<ApplicationStatusCount> getApplicationsByStatus() { return applicationsByStatus; }
        public void setApplicationsByStatus(List<ApplicationStatusCount> applicationsByStatus) {
            this.applicationsByStatus = applicationsByStatus;
        }
        public List<ApplicationDtos.ApplicationResponse> getRecentApplications() { return recentApplications; }
        public void setRecentApplications(List<ApplicationDtos.ApplicationResponse> recentApplications) {
            this.recentApplications = recentApplications;
        }
        public List<JobApplicantCount> getApplicantsPerJob() { return applicantsPerJob; }
        public void setApplicantsPerJob(List<JobApplicantCount> applicantsPerJob) {
            this.applicantsPerJob = applicantsPerJob;
        }
        public List<JobApplicantCount> getTopJobsByApplicants() { return topJobsByApplicants; }
        public void setTopJobsByApplicants(List<JobApplicantCount> topJobsByApplicants) {
            this.topJobsByApplicants = topJobsByApplicants;
        }
    }
}
