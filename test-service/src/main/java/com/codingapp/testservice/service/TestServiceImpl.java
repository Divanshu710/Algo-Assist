package com.codingapp.testservice.service;

import com.codingapp.testservice.client.ProblemClient;
import com.codingapp.testservice.client.UserClient;
import com.codingapp.testservice.dto.ApiResponse;
import com.codingapp.testservice.dto.CreateTestRequest;
import com.codingapp.testservice.dto.ProblemDto;
import com.codingapp.testservice.dto.TestResponse;
import com.codingapp.testservice.exception.InvalidTestStateException;
import com.codingapp.testservice.exception.ResourceNotFoundException;
import com.codingapp.testservice.model.Test;
import com.codingapp.testservice.model.TestStatus;
import com.codingapp.testservice.repository.TestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final TestRepository testRepository;
    private final UserClient userClient;
    private final ProblemClient problemClient;

    @Override
    @Transactional
    public TestResponse createTest(CreateTestRequest request, String creatorId) {
        // 1. Verify the invitee email exists AND get their User ID from user-service
        ApiResponse<String> userRes = userClient.getUserIdByEmail(request.getInviteeEmail());
        if (userRes == null || !userRes.isSuccess() || userRes.getData() == null) {
            throw new ResourceNotFoundException("Invitee email does not exist in the system.");
        }
        String fetchedInviteeId = userRes.getData(); // We now have the ID!

        // 2. Fetch all problem details to calculate the totalMaxScore
        int calculatedMaxScore = 0;
        for (Long problemId : request.getProblemIds()) {
            ApiResponse<ProblemDto> problemRes = problemClient.getProblemById(problemId);
            if (problemRes == null || !problemRes.isSuccess() || problemRes.getData() == null) {
                throw new ResourceNotFoundException("Problem ID " + problemId + " does not exist.");
            }
            calculatedMaxScore += problemRes.getData().getMaxScore();
        }

        // 3. Build and save the Test entity using inviteeId instead of inviteeEmail
        Test test = Test.builder()
                .title(request.getTitle())
                .creatorId(creatorId)
                .inviteeId(fetchedInviteeId)
                .durationMinutes(request.getDurationMinutes())
                .status(TestStatus.PENDING)
                .problemIds(request.getProblemIds())
                .totalMaxScore(calculatedMaxScore)
                .build();

        test = testRepository.save(test);
        return mapToTestResponse(test);
    }

    @Override
    @Transactional
    public TestResponse startTest(Long testId, String requestorId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        // Security check: Only the assigned invitee ID can start the test
        if (!test.getInviteeId().equals(requestorId)) {
            throw new InvalidTestStateException("You are not authorized to take this test.");
        }

        // State machine check: Can only start a PENDING test
        if (test.getStatus() != TestStatus.PENDING) {
            throw new InvalidTestStateException("Test has already been started or completed.");
        }

        // Start the strict server-side timer
        test.setStatus(TestStatus.IN_PROGRESS);
        test.setStartTime(LocalDateTime.now());
        test.setEndTime(LocalDateTime.now().plusMinutes(test.getDurationMinutes()));

        testRepository.save(test);
        return mapToTestResponse(test);
    }

    @Override
    @Transactional
    public TestResponse getTestDetails(Long testId, String requestorId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        // Security check: Only creator or assigned invitee ID can view this test
        if (!test.getCreatorId().equals(requestorId) && !test.getInviteeId().equals(requestorId)) {
            throw new InvalidTestStateException("Not authorized to view this test.");
        }

        // AUTO-EXPIRY CHECK: Clean up if timer ran out without submission
        checkAndHandleExpiry(test);

        return mapToTestResponse(test);
    }

    @Override
    @Transactional
    public List<TestResponse> getTestsForInvitee(String inviteeId) {
        // Querying the repository by ID now
        List<Test> tests = testRepository.findByInviteeId(inviteeId);

        // AUTO-EXPIRY CHECK: Clean up abandoned tests before showing them on the dashboard
        tests.forEach(this::checkAndHandleExpiry);

        return tests.stream().map(this::mapToTestResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<TestResponse> getTestsCreatedBy(String creatorId) {
        List<Test> tests = testRepository.findByCreatorId(creatorId);

        // AUTO-EXPIRY CHECK: Ensure the creator sees accurate statuses
        tests.forEach(this::checkAndHandleExpiry);

        return tests.stream().map(this::mapToTestResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void submitTest(Long testId, String requestorId) {
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found"));

        // Security check using ID
        if (!test.getInviteeId().equals(requestorId)) {
            throw new InvalidTestStateException("You are not authorized to submit this test.");
        }

        // AUTO-EXPIRY CHECK: Did they try to submit after the timer hit 0?
        if (test.getStatus() == TestStatus.IN_PROGRESS && LocalDateTime.now().isAfter(test.getEndTime())) {
            test.setStatus(TestStatus.EXPIRED);
            testRepository.save(test);
            throw new InvalidTestStateException("Time is up! Your test has been automatically expired.");
        }

        if (test.getStatus() != TestStatus.IN_PROGRESS) {
            throw new InvalidTestStateException("Can only submit tests that are currently in progress.");
        }

        // User clicked "End Test" gracefully before time ran out
        test.setStatus(TestStatus.COMPLETED);

        // Future Integration: Call submission-service here to calculate 'scoreAchieved'

        testRepository.save(test);
    }

    // --- Helper Methods ---

    private void checkAndHandleExpiry(Test test) {
        if (test.getStatus() == TestStatus.IN_PROGRESS && LocalDateTime.now().isAfter(test.getEndTime())) {
            test.setStatus(TestStatus.EXPIRED);
            // Future Integration: Call submission-service to calculate score up to the expiry moment
            testRepository.save(test);
        }
    }

    private TestResponse mapToTestResponse(Test test) {
        List<ProblemDto> problemDtos = new ArrayList<>();

        // Dynamically enrich problem data via Feign Client
        for (Long pid : test.getProblemIds()) {
            try {
                ApiResponse<ProblemDto> response = problemClient.getProblemById(pid);
                if (response != null && response.getData() != null) {
                    problemDtos.add(response.getData());
                }
            } catch (Exception e) {
                // Fallback to prevent breaking the UI if problem-service is temporarily down
                problemDtos.add(ProblemDto.builder().id(pid).title("Problem details temporarily unavailable").build());
            }
        }

        return TestResponse.builder()
                .id(test.getId())
                .title(test.getTitle())
                .creatorId(test.getCreatorId())
                .inviteeId(test.getInviteeId()) // Mapped the ID here
                .durationMinutes(test.getDurationMinutes())
                .status(test.getStatus())
                .startTime(test.getStartTime())
                .endTime(test.getEndTime())
                .problems(problemDtos)
                .createdAt(test.getCreatedAt())
                .build();
    }
}