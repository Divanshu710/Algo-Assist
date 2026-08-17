package com.codingapp.submissionservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Judge0Response {

    private String stdout;
    private String stderr;

    @JsonProperty("compile_output")
    private String compileOutput;

    private String time;
    private Double memory;

    private Judge0Status status;

    // A nested class just to grab the status ID and description
    @Data
    public static class Judge0Status {
        private Integer id;
        private String description;
    }
}