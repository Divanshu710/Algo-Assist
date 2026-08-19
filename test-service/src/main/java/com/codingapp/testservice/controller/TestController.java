package com.codingapp.testservice.controller;

import com.codingapp.testservice.dto.ApiResponse;
import com.codingapp.testservice.dto.CreateTestRequest;
import com.codingapp.testservice.dto.TestResponse;
import com.codingapp.testservice.service.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/test-service/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    @PostMapping
    public ResponseEntity<ApiResponse<TestResponse>> createTest(
            @Valid @RequestBody CreateTestRequest request,
            @RequestHeader("X-User-Id") String creatorId) {

        TestResponse testResponse = testService.createTest(request, creatorId);

        return new ResponseEntity<>(ApiResponse.<TestResponse>builder()
                .success(true)
                .message("Test created successfully")
                .data(testResponse)
                .build(), HttpStatus.CREATED);
    }

    @GetMapping("/created")
    public ResponseEntity<ApiResponse<List<TestResponse>>> getTestsCreatedBy(
            @RequestHeader("X-User-Id") String creatorId) {

        List<TestResponse> tests = testService.getTestsCreatedBy(creatorId);

        return ResponseEntity.ok(ApiResponse.<List<TestResponse>>builder()
                .success(true)
                .message("Fetched created tests")
                .data(tests)
                .build());
    }

    @GetMapping("/assigned")
    public ResponseEntity<ApiResponse<List<TestResponse>>> getTestsForInvitee(
            @RequestHeader("X-User-Id") String inviteeId) {

        List<TestResponse> tests = testService.getTestsForInvitee(inviteeId);

        return ResponseEntity.ok(ApiResponse.<List<TestResponse>>builder()
                .success(true)
                .message("Fetched assigned tests")
                .data(tests)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TestResponse>> getTestDetails(
            @PathVariable("id") Long testId,
            @RequestHeader("X-User-Id") String userId) {

        TestResponse testResponse = testService.getTestDetails(testId, userId);

        return ResponseEntity.ok(ApiResponse.<TestResponse>builder()
                .success(true)
                .message("Fetched test details")
                .data(testResponse)
                .build());
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<ApiResponse<TestResponse>> startTest(
            @PathVariable("id") Long testId,
            @RequestHeader("X-User-Id") String requestorId) {

        TestResponse testResponse = testService.startTest(testId, requestorId);

        return ResponseEntity.ok(ApiResponse.<TestResponse>builder()
                .success(true)
                .message("Test started successfully. Timer is now running.")
                .data(testResponse)
                .build());
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApiResponse<Void>> submitTest(
            @PathVariable("id") Long testId,
            @RequestHeader("X-User-Id") String requestorId) {

        testService.submitTest(testId, requestorId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Test submitted successfully")
                .build());
    }
}