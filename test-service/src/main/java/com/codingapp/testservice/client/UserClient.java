package com.codingapp.testservice.client;

import com.codingapp.testservice.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Point this URL to your user-service port
@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserClient {

    @GetMapping("/api/v1/user-service/internal/users/exists")
    ApiResponse<Boolean> checkUserExistsByEmail(@RequestParam("email") String email);

    // UserClient.java
    @GetMapping("/api/v1/user-service/internal/users/get-id")
    ApiResponse<String> getUserIdByEmail(@RequestParam("email") String email);

}