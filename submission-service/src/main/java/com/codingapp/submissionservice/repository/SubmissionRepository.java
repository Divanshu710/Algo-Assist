package com.codingapp.submissionservice.repository;

import com.codingapp.submissionservice.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    // Spring Boot magically writes the SQL for this just by reading the method name!
    // It translates to: SELECT * FROM submissions WHERE user_id = ?
    List<Submission> findByUserId(String userId);

    boolean existsByUserIdAndProblemIdAndVerdict(String userId, Long problemId, String verdict);
}