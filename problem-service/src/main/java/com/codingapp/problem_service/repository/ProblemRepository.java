package com.codingapp.problem_service.repository;

import com.codingapp.problem_service.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProblemRepository extends JpaRepository<Problem, Long> {

    List<Problem> findByAuthorId(Long authorId);

}
