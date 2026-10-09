package com.jobportal.repository;

import com.jobportal.entity.ApplicationStatus;
import com.jobportal.entity.JobApplication;
import com.jobportal.entity.JobStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findByJobIdOrderByAppliedAtDesc(Long jobId);

    List<JobApplication> findByCandidateIdOrderByAppliedAtDesc(Long candidateId);

    Optional<JobApplication> findByJobIdAndCandidateId(Long jobId, Long candidateId);

    Optional<JobApplication> findByIdAndCandidateId(Long id, Long candidateId);

    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);

    long countByJobId(Long jobId);

    long countByJobIdAndStatus(Long jobId, ApplicationStatus status);

    long countByCandidateId(Long candidateId);

    // --- Dashboard aggregations (counts computed in the database) ---

    @Query("""
            SELECT a.status AS status, COUNT(a) AS total
            FROM JobApplication a
            WHERE a.candidateId = :candidateId
            GROUP BY a.status
            """)
    List<ApplicationStatusCount> countApplicationsByStatusForCandidate(
            @Param("candidateId") Long candidateId);

    List<JobApplication> findTop5ByCandidateIdOrderByAppliedAtDescIdDesc(Long candidateId);

    List<JobApplication> findTop5ByCandidateIdOrderByUpdatedAtDescIdDesc(Long candidateId);

    @Query("""
            SELECT a.status AS status, COUNT(a) AS total
            FROM JobApplication a
            WHERE a.jobId IN (SELECT j.id FROM Job j WHERE j.postedByUserId = :hrUserId)
            GROUP BY a.status
            """)
    List<ApplicationStatusCount> countApplicationsByStatusForOwner(@Param("hrUserId") Long hrUserId);

    @Query("""
            SELECT a FROM JobApplication a
            WHERE a.jobId IN (SELECT j.id FROM Job j WHERE j.postedByUserId = :hrUserId)
            ORDER BY a.appliedAt DESC, a.id DESC
            """)
    List<JobApplication> findRecentByOwner(@Param("hrUserId") Long hrUserId, Pageable pageable);

    @Query("""
            SELECT a.jobId AS jobId, j.title AS jobTitle, j.status AS status, COUNT(a) AS total
            FROM JobApplication a, Job j
            WHERE j.id = a.jobId AND j.postedByUserId = :hrUserId
            GROUP BY a.jobId, j.title, j.status
            ORDER BY COUNT(a) DESC, a.jobId ASC
            """)
    List<JobApplicantCount> findApplicantCountsByOwner(@Param("hrUserId") Long hrUserId);

    interface ApplicationStatusCount {
        ApplicationStatus getStatus();

        long getTotal();
    }

    interface JobApplicantCount {
        Long getJobId();

        String getJobTitle();

        JobStatus getStatus();

        long getTotal();
    }
}
