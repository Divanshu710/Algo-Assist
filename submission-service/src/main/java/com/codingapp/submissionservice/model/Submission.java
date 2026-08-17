package com.codingapp.submissionservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "submissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "submission_id", updatable = false, nullable = false)
    private UUID submissionId;

    // Extracted from the API Gateway's JWT (X-User-Id)
    @Column(name = "user_id", nullable = false)
    private String userId;

    // References the problem in the problem-service
    @Column(name = "problem_id", nullable = false)
    private Long problemId;

    // Judge0's specific ID for the language (e.g., 62 for Java, 71 for Python)
    @Column(name = "language_id", nullable = false)
    private Integer languageId;

    // Uses TEXT because source code can easily exceed the default varchar(255) limit
    @Column(name = "source_code", nullable = false, columnDefinition = "TEXT")
    private String sourceCode;

    // e.g., "Accepted", "Wrong Answer", "Time Limit Exceeded", "Compilation Error"
    @Column(nullable = false)
    private String verdict;

    @Column(name = "execution_time_ms")
    private Double executionTimeMs;

    @Column(name = "memory_used_kb")
    private Double memoryUsedKb;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}