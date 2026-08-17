package com.codingapp.submissionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProblemDto {

    private long id;
    private String difficulty;
    private Integer timeLimitMs;
    private Integer memoryLimitMb;
    private List<TestCaseDto> testCases;


}
