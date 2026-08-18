package com.codingapp.problem_service.dto;

import com.codingapp.problem_service.model.Difficulty;
import com.codingapp.problem_service.model.TestCase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProblemResponse implements Serializable {

    private Long id;
    private String title;
    private String description;
    private Difficulty difficulty;
    private Integer timeLimitMs;
    private Integer memoryLimitMb;
    private List<TestCase> testCases;
    private String authorId;

}