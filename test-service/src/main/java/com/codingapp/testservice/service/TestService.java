package com.codingapp.testservice.service;

import com.codingapp.testservice.dto.CreateTestRequest;
import com.codingapp.testservice.dto.TestResponse;

import java.util.List;

public interface TestService {
    TestResponse createTest(CreateTestRequest request, String creatorId);
    TestResponse startTest(Long testId, String requestorId);
    TestResponse getTestDetails(Long testId, String requestorId);
    List<TestResponse> getTestsForInvitee(String inviteeId);
    void submitTest(Long testId, String requestorId);
    List<TestResponse> getTestsCreatedBy(String creatorId);
}