package com.codingapp.testservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemDto {
    private Long id;
    private String title;

    // Removed difficulty!

    // If a problem has 10 total test cases, maxScore is 10.
    private Integer maxScore;

    // This will only contain the 2-3 visible test cases
    private List<TestCaseDto> sampleTestCases;
}