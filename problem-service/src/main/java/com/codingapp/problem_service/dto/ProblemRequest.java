package com.codingapp.problem_service.dto;

import com.codingapp.problem_service.model.Difficulty;
import com.codingapp.problem_service.model.TestCase;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemRequest {

    @NotBlank(message = "Title cannot be blank")
    private String title;

    @NotBlank(message = "Description cannot be blank")
    private String description;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    @NotNull(message = "Time limit is required")
    @Min(value = 100, message = "Time limit must be at least 100ms")
    private Integer timeLimitMs;

    @NotNull(message = "Memory limit is required")
    @Min(value = 16, message = "Memory limit must be at least 16MB")
    private Integer memoryLimitMb;

    @NotNull(message = "Test cases are required")
    private List<TestCase> testCases;
}