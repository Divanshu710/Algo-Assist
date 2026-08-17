package com.codingapp.submissionservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Judge0Request {

    @JsonProperty("source_code")
    private String sourceCode;

    @JsonProperty("language_id")
    private Integer languageId;

    @JsonProperty("stdin")
    private String stdin;

    @JsonProperty("expected_output")
    private String expectedOutput;

    // Time limit in seconds (Judge0 expects a Float, e.g., 2.0 for 2 seconds)
    @JsonProperty("cpu_time_limit")
    private Float cpuTimeLimit;

    // Memory limit in kilobytes
    @JsonProperty("memory_limit")
    private Float memoryLimit;
}