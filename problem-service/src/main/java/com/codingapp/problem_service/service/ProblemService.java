package com.codingapp.problem_service.service;

import com.codingapp.problem_service.dto.ProblemRequest;
import com.codingapp.problem_service.dto.ProblemResponse;
import com.codingapp.problem_service.exception.ResourceNotFoundException;
import com.codingapp.problem_service.model.Problem;
import com.codingapp.problem_service.model.TestCase;
import com.codingapp.problem_service.repository.ProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProblemService {

    private final ProblemRepository problemRepository;

    @CacheEvict(value="allProblems", allEntries=true)
    public ProblemResponse createProblem(ProblemRequest request, String authorId) {

        if (problemRepository.existsByTitle(request.getTitle())) {
            throw new ResourceNotFoundException("Title already exists");
        }
        Problem problem = Problem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .difficulty(request.getDifficulty())
                .timeLimitMs(request.getTimeLimitMs())
                .memoryLimitMb(request.getMemoryLimitMb())
                .testCases(request.getTestCases())
                .authorId(authorId)
                .build();

        Problem savedProblem = problemRepository.save(problem);
        return mapToProblemResponse(savedProblem);
    }

    @Cacheable(value="problem", key="#id")
    public ProblemResponse getProblemById(Long id){
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));

        return mapToProblemResponse(problem);
    }

    @Cacheable(value = "allProblems")
    public List<ProblemResponse> getAllProblems(){

        return problemRepository.findAll()
                .stream()
                .map(this::mapToProblemResponse)
                .collect(Collectors.toList());
    }


    private ProblemResponse mapToProblemResponse(Problem problem) {
        int numberOfSampleCases = Math.min(problem.getTestCases().size(), 2);
        List<TestCase> sampleTestCases = problem.getTestCases().subList(0, numberOfSampleCases);

        return ProblemResponse.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .difficulty(problem.getDifficulty())
                .timeLimitMs(problem.getTimeLimitMs())
                .memoryLimitMb(problem.getMemoryLimitMb())
                .testCases(sampleTestCases)
                .authorId(problem.getAuthorId())
                .build();

    }

    // ... your existing methods ...

    public ProblemResponse getFullProblemByIdInternal(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));

        // Return the response with ALL test cases (No subList!)
        return ProblemResponse.builder()
                .id(problem.getId())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .difficulty(problem.getDifficulty())
                .timeLimitMs(problem.getTimeLimitMs())
                .memoryLimitMb(problem.getMemoryLimitMb())
                .testCases(new ArrayList<>(problem.getTestCases())) // Sends every single test case!
                .authorId(problem.getAuthorId())
                .build();
    }
}
