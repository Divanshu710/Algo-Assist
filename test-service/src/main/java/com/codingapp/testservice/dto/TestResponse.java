package com.codingapp.testservice.dto;

import com.codingapp.testservice.model.TestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestResponse {
    private Long id;
    private String title;
    private String creatorId;
    private String inviteeId;
    private Integer durationMinutes;
    private TestStatus status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    // Notice this is a List of DTOs, not raw Longs!
    private List<ProblemDto> problems;

    private LocalDateTime createdAt;
}