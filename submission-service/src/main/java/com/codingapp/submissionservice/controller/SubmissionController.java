package com.codingapp.submissionservice.controller;

import com.codingapp.submissionservice.dto.*;
import com.codingapp.submissionservice.model.Submission;
import com.codingapp.submissionservice.repository.SubmissionRepository;
import com.codingapp.submissionservice.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/submission-service")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final SubmissionRepository submissionRepository;

    /**
     * The SUBMIT button endpoint (Executes against all hidden test cases)
     */
    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<SubmissionResponse>> submitCode(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody SubmissionRequest request) {

        log.info("Received code submission request from user: {}", userId);

        // Hand off to our orchestration service
        SubmissionResponse response = submissionService.submitCode(request, userId);

        // Wrap the result in our standard ApiResponse
        return ResponseEntity.ok(ApiResponse.<SubmissionResponse>builder()
                .success(true)
                .message("Code submitted and evaluated successfully")
                .data(response)
                .build());
    }

    /**
     * The RUN button endpoint (Executes a single custom test case)
     */
    @PostMapping("/run")
    public ResponseEntity<ApiResponse<Judge0Response>> runCode(
            @Valid @RequestBody RunRequest request) {

        log.info("Received custom run request");

        // Execute the code and get the raw response
        Judge0Response response = submissionService.runCode(request);

        return ResponseEntity.ok(ApiResponse.<Judge0Response>builder()
                .success(true)
                .message("Custom code executed successfully")
                .data(response)
                .build());
    }

    /**
     * Fetches the entire submission history for the logged-in user
     */
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<Submission>>> getUserSubmissions(
            @RequestHeader("X-User-Id") String userId) {

        log.info("Fetching submission history for user: {}", userId);


        List<Submission> submissions = submissionService.getUserSubmissions(userId);

        return ResponseEntity.ok(ApiResponse.<List<Submission>>builder()
                .success(true)
                .message("User submissions fetched successfully")
                .data(submissions)
                .build());
    }

    /**
     * Fetches the submission history for the logged-in user for a SPECIFIC problem
     */
    @GetMapping("/user/problem/{problemId}")
    public ResponseEntity<ApiResponse<List<Submission>>> getUserSubmissionsForProblem(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable Long problemId) {

        log.info("Fetching submission history for user: {} and problem: {}", userId, problemId);

        List<Submission> submissions = submissionService.getUserSubmissionsForProblem(userId, problemId);

        return ResponseEntity.ok(ApiResponse.<List<Submission>>builder()
                .success(true)
                .message("Problem specific submissions fetched successfully")
                .data(submissions)
                .build());
    }
}