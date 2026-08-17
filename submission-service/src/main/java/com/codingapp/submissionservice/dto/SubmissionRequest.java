package com.codingapp.submissionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionRequest {

    @NotNull(message = "Problem ID is required")
    private Long problemId;

    @NotNull(message = "Language ID is required")
    private Integer languageId;

    @NotBlank(message = "Source code cannot be blank")
    private String sourceCode;
}