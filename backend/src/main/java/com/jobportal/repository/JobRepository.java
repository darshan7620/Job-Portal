package com.jobportal.repository;

import com.jobportal.entity.Job;
import com.jobportal.entity.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {

    List<Job> findByStatusOrderByCreatedAtDesc(JobStatus status);

    List<Job> findByPostedByUserIdOrderByCreatedAtDesc(Long postedByUserId);

    Optional<Job> findByIdAndPostedByUserId(Long id, Long postedByUserId);

    long countByPostedByUserId(Long postedByUserId);

    @Query("""
            SELECT j.status AS status, COUNT(j) AS total
            FROM Job j
            WHERE j.postedByUserId = :hrUserId
            GROUP BY j.status
            """)
    List<JobStatusCount> countJobsByStatusForOwner(@Param("hrUserId") Long hrUserId);

    interface JobStatusCount {
        JobStatus getStatus();

        long getTotal();
    }

    @Query("""
            SELECT j FROM Job j
            WHERE j.status = com.jobportal.entity.JobStatus.PUBLISHED
              AND (:keyword IS NULL OR :keyword = ''
                   OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(j.company) LIKE LOWER(CONCAT('%', :keyword, '%'))
                   OR LOWER(j.requiredSkills) LIKE LOWER(CONCAT('%', :keyword, '%')))
              AND (:location IS NULL OR :location = ''
                   OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%')))
              AND (:employmentType IS NULL OR :employmentType = ''
                   OR LOWER(j.employmentType) = LOWER(:employmentType))
            """)
    Page<Job> searchPublished(@Param("keyword") String keyword,
                              @Param("location") String location,
                              @Param("employmentType") String employmentType,
                              org.springframework.data.domain.Pageable pageable);
}
