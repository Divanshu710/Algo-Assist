package com.codingapp.problem_service.controller;

import com.codingapp.problem_service.dto.ApiResponse;
import com.codingapp.problem_service.dto.ProblemRequest;
import com.codingapp.problem_service.dto.ProblemResponse;
import com.codingapp.problem_service.service.ProblemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/problem-service")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    // 1. Create a new problem
    @PostMapping("/create-problem")
    public ResponseEntity<ApiResponse<ProblemResponse>> createProblem(
            @Valid @RequestBody ProblemRequest request,
            @RequestHeader("X-User-Id") String authorId) { // Safely extracted from API Gateway

        ProblemResponse createdProblem = problemService.createProblem(request, authorId);

        ApiResponse<ProblemResponse> response = new ApiResponse<>(
                true,
                "Problem created successfully",
                createdProblem,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // 2. Get a problem by ID
    @GetMapping("/problem/{id}")
    public ResponseEntity<ApiResponse<ProblemResponse>> getProblemById(@PathVariable Long id) {

        ProblemResponse problem = problemService.getProblemById(id);

        ApiResponse<ProblemResponse> response = new ApiResponse<>(
                true,
                "Problem fetched successfully",
                problem,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }

    // 3. Get all problems
    @GetMapping("/problems")
    public ResponseEntity<ApiResponse<List<ProblemResponse>>> getAllProblems() {

        List<ProblemResponse> problems = problemService.getAllProblems();

        ApiResponse<List<ProblemResponse>> response = new ApiResponse<>(
                true,
                "Problems fetched successfully",
                problems,
                LocalDateTime.now()
        );

        return ResponseEntity.ok(response);
    }
}