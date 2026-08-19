package com.codingapp.userservice.controller;

import com.codingapp.userservice.dto.ApiResponse;
import com.codingapp.userservice.dto.UserProfileResponse;
import com.codingapp.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/internal/users/exists")
    public ApiResponse<Boolean> checkUserExistsByEmail(@RequestParam("email") String email){
        boolean res = userService.doEmailExists(email);

        ApiResponse<Boolean> response = ApiResponse.<Boolean>builder()
                .success(true)
                .message("Details fetched")
                .data(res)
                .build();

        return response;
    }

    @GetMapping("/internal/users/get-id")
    public ApiResponse<String> getUserIdByEmail(@RequestParam("email") String email) {

        String userId = userService.getUserIdByEmail(email);

        return ApiResponse.<String>builder()
                .success(true)
                .message("User ID fetched successfully")
                .data(userId)
                .build();
    }
}
