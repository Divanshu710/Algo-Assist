package com.codingapp.submissionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionResponse {
    private UUID submissionId;
    private Long problemId;
    private String verdict;          // e.g., "Accepted", "Wrong Answer"
    private Double executionTimeMs;
    private Double memoryUsedKb;
}