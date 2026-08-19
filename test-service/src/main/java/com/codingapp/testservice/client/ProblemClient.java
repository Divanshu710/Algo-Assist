package com.codingapp.testservice.client;

import com.codingapp.testservice.dto.ApiResponse;
import com.codingapp.testservice.dto.ProblemDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// If you are using Eureka Service Discovery, you can remove the 'url' parameter.
// Otherwise, keep the hardcoded localhost URL for your problem-service port.
@FeignClient(name = "problem-service", url = "${problem-service.url}")
public interface ProblemClient {

    @GetMapping("/api/v1/problem-service/internal/problem/{id}")
    ApiResponse<ProblemDto> getProblemById(@PathVariable("id") Long id);

}