package com.codingapp.problem_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestCase implements Serializable {
    private String input;
    private String expectedOutput;
}