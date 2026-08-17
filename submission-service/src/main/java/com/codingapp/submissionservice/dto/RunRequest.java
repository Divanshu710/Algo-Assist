package com.codingapp.submissionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RunRequest {

    @NotNull(message = "Language ID is required")
    private Integer languageId;

    @NotBlank(message = "Source code cannot be blank")
    private String sourceCode;

    // The custom test case typed by the user
    private String customInput;
}