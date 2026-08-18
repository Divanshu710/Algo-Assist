package com.codingapp.userservice.controller;

import com.codingapp.userservice.dto.ApiResponse;
import com.codingapp.userservice.dto.UserProfileResponse;
import com.codingapp.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user-service")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getUser(@RequestHeader("X-User-Id") String userId){
        UserProfileResponse response = userService.getUserProfile(userId);

        ApiResponse<UserProfileResponse> res = ApiResponse.<UserProfileResponse>builder()
                .success(true)
                .message("Profile fetched successfully")
                .data(response)
                .build();

        return ResponseEntity.ok(res);
    }
}
