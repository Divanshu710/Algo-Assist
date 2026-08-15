package com.codingapp.problem_service.dto;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();


}
