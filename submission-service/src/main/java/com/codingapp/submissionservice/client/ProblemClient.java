package com.codingapp.submissionservice.client;


import com.codingapp.submissionservice.dto.ApiResponse;
import com.codingapp.submissionservice.dto.ProblemDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name="problem-service", url="${problem-service.url}")
public interface ProblemClient {

    @GetMapping("/api/v1/problem-service/problem/{id}")
    ApiResponse<ProblemDto> getProblem(@PathVariable("id") Long id);

}
