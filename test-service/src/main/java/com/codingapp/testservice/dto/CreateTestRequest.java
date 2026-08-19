package com.codingapp.testservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateTestRequest {

    @NotBlank(message = "Test title is required")
    private String title;

    @NotBlank(message = "Invitee email is required")
    @Email(message = "Must be a valid email address")
    private String inviteeEmail;

    @Min(value = 5, message = "Test duration must be at least 5 minutes")
    private Integer durationMinutes;

    @NotEmpty(message = "A test must contain at least one problem")
    private List<Long> problemIds;
}