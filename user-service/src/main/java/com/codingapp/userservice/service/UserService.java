package com.codingapp.userservice.service;

import com.codingapp.userservice.dto.UserProfileResponse;
import com.codingapp.userservice.exception.UserNotFoundException;
import com.codingapp.userservice.model.User;
import com.codingapp.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * @Cacheable tells Spring:
     * 1. Check Redis for a "userProfiles" bucket with the key (the userId).
     * 2. If it exists, return it instantly (DO NOT run this method).
     * 3. If it doesn't exist, run this method, hit PostgreSQL, return the data, AND save it to Redis.
     */
    @Cacheable(value = "userProfiles", key = "#userId")
    public UserProfileResponse getUserProfile(String userId) {
        log.info("CACHE MISS! Fetching user profile from PostgreSQL for user: {}", userId);

        UUID userUuid = UUID.fromString(userId);
        User user = userRepository.findById(userUuid)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        int totalSolved = user.getEasySolved() + user.getMediumSolved() + user.getHardSolved();

        return UserProfileResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .userTier(user.getUserTier())
                .easySolved(user.getEasySolved())
                .mediumSolved(user.getMediumSolved())
                .hardSolved(user.getHardSolved())
                .totalSolved(totalSolved)
                .build();
    }
}