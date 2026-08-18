package com.codingapp.submissionservice.service;

import com.codingapp.submissionservice.client.ProblemClient;
import com.codingapp.submissionservice.dto.*;
import com.codingapp.submissionservice.exception.ExternalServiceException;
import com.codingapp.submissionservice.exception.ResourceNotFoundException;
import com.codingapp.submissionservice.executor.Judge0Service;
import com.codingapp.submissionservice.model.Submission;
import com.codingapp.submissionservice.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final Judge0Service judge0Service;
    private final ProblemClient problemClient;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Caching(evict = {
            @CacheEvict(value = "userSubmissions", key = "#userId"),
            @CacheEvict(value = "userProblemSubmissions", key = "#userId + '-' + #request.problemId")
    })
    public SubmissionResponse submitCode(SubmissionRequest request, String userId) {
        log.info("Processing submission for user {} and problem {}", userId, request.getProblemId());

        // Step 1: Fetch Problem Details and Test Cases
        ApiResponse<ProblemDto> problemResponse;
        try {
            problemResponse = problemClient.getProblem(request.getProblemId());
        } catch (Exception e) {
            log.error("Failed to connect to problem-service", e);
            throw new ExternalServiceException("Failed to fetch problem details. The problem service might be down.");
        }

        if (!problemResponse.isSuccess() || problemResponse.getData() == null) {
            throw new ResourceNotFoundException("Problem with ID " + request.getProblemId() + " not found");
        }

        ProblemDto problem = problemResponse.getData();
        List<TestCaseDto> testCases = problem.getTestCases();

        if (testCases == null || testCases.isEmpty()) {
            throw new ResourceNotFoundException("No test cases found for problem ID " + request.getProblemId());
        }

        // Step 2: Prepare the Batch Requests for Judge0
        // Convert problem limits from MS/MB (Database) to Seconds/KB (Judge0 requires this)
        Float cpuTimeLimit = problem.getTimeLimitMs() / 1000.0f;
        Float memoryLimitKb = problem.getMemoryLimitMb() * 1024.0f;

        List<Judge0Request> judge0Requests = new ArrayList<>();
        for (TestCaseDto tc : testCases) {
            Judge0Request jReq = Judge0Request.builder()
                    .sourceCode(request.getSourceCode())
                    .languageId(request.getLanguageId())
                    .stdin(tc.getInput())
                    .expectedOutput(tc.getExpectedOutput())
                    .cpuTimeLimit(cpuTimeLimit)
                    .memoryLimit(memoryLimitKb)
                    .build();
            judge0Requests.add(jReq);
        }

        // Step 3: Send to Judge0 and Wait for Results
        List<Judge0Response> judge0Responses;
        try {
            judge0Responses = judge0Service.submitBatchTests(judge0Requests);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalServiceException("Code execution was interrupted while waiting for Judge0");
        } catch (Exception e) {
            log.error("Judge0 execution failed", e);
            throw new ExternalServiceException("Failed to execute code on Judge0 compiler");
        }

        // Step 4: Evaluate the Final Verdict
        String finalVerdict = "Accepted";
        double maxTime = 0.0;
        double maxMemory = 0.0;

        for (Judge0Response response : judge0Responses) {
            // Track the maximum time and memory used across all instances
            if (response.getTime() != null) {
                double timeInMs = Double.parseDouble(response.getTime()) * 1000;
                maxTime = Math.max(maxTime, timeInMs);
            }
            if (response.getMemory() != null) {
                maxMemory = Math.max(maxMemory, response.getMemory());
            }

            // Judge0 Status ID 3 means "Accepted". Anything else is a failure.
            int statusId = response.getStatus() != null ? response.getStatus().getId() : 13;
            if (statusId != 3) {
                finalVerdict = resolveJudge0Status(statusId);
                break; // Stop checking further test cases once one fails
            }
        }


        // --- NEW LOGIC: Check if it was already solved BEFORE saving the new attempt ---
        boolean isFirstTimeSolve = false;

        if ("Accepted".equals(finalVerdict)) {
            boolean alreadySolved = submissionRepository.existsByUserIdAndProblemIdAndVerdict(
                    userId, request.getProblemId(), "Accepted");

            // If they haven't solved it before, this is their first time!
            isFirstTimeSolve = !alreadySolved;
        }

        // Step 5: Save to our PostgreSQL Database
        Submission submission = Submission.builder()
                .userId(userId)
                .problemId(request.getProblemId())
                .languageId(request.getLanguageId())
                .sourceCode(request.getSourceCode())
                .verdict(finalVerdict)
                .executionTimeMs(maxTime)
                .memoryUsedKb(maxMemory)
                .build();

        submission = submissionRepository.save(submission);

        // Step 5.5: Broadcast the event ONLY if it is a unique, first-time solve
        if (isFirstTimeSolve) {
            ProblemSolvedEvent event = ProblemSolvedEvent.builder()
                    .userId(userId)
                    .problemId(request.getProblemId())
                    .difficulty(problem.getDifficulty())
                    .build();

            log.info("First time solve! Publishing ProblemSolvedEvent to Kafka for user: {}", userId);
            kafkaTemplate.send("problem-solved-topic", event);
        } else if ("Accepted".equals(finalVerdict)) {
            log.info("User {} already solved problem {}. Skipping Kafka event.", userId, request.getProblemId());
        }

        // Step 6: Return the clean response to the controller
        return SubmissionResponse.builder()
                .submissionId(submission.getSubmissionId())
                .problemId(submission.getProblemId())
                .verdict(submission.getVerdict())
                .executionTimeMs(maxTime)
                .memoryUsedKb(maxMemory)
                .build();
    }

    /**
     * Executes code against a single custom input without saving to the database
     */
    public Judge0Response runCode(RunRequest request) {
        log.info("Running custom test case for language ID: {}", request.getLanguageId());

        // We set generous default limits for custom test runs
        Judge0Request judge0Request = Judge0Request.builder()
                .sourceCode(request.getSourceCode())
                .languageId(request.getLanguageId())
                .stdin(request.getCustomInput() != null ? request.getCustomInput() : "")
                .cpuTimeLimit(5.0f)     // 5 seconds max
                .memoryLimit(256000.0f) // 256 MB max
                .build();

        try {
            // Use the single-test method we built earlier (with wait=true)
            return judge0Service.runSingleTest(judge0Request);
        } catch (Exception e) {
            log.error("Judge0 execution failed for custom run", e);
            throw new ExternalServiceException("Failed to execute custom test case on Judge0");
        }
    }

    @Cacheable(value = "userProblemSubmissions", key = "#userId + '-' + #problemId")
    public List<Submission> getUserSubmissionsForProblem(String userId, Long problemId) {
        log.info("CACHE MISS! Fetching submissions for user: {} and problem: {}", userId, problemId);
        return submissionRepository.findByUserIdAndProblemIdOrderByCreatedAtDesc(userId, problemId);
    }

    @Cacheable(value = "userSubmissions", key = "#userId")
    public List<Submission> getUserSubmissions(String userId) {
        log.info("CACHE MISS! Fetching submission history from PostgreSQL for user: {}", userId);
        return submissionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Helper method to convert Judge0 status IDs to readable frontend strings
     */
    private String resolveJudge0Status(int statusId) {
        return switch (statusId) {
            case 4 -> "Wrong Answer";
            case 5 -> "Time Limit Exceeded";
            case 6 -> "Compilation Error";
            case 7, 8, 9, 10, 11, 12 -> "Runtime Error";
            case 13, 14 -> "Internal Error";
            default -> "Error Code: " + statusId;
        };
    }
}