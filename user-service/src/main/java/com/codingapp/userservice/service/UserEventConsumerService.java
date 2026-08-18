package com.codingapp.userservice.service;

import com.codingapp.userservice.dto.ProblemSolvedEvent;
import com.codingapp.userservice.model.User;
import com.codingapp.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventConsumerService {

    private final UserRepository userRepository;

    /**
     * Listens to the "problem-solved-topic".
     * Whenever a message arrives, Spring Boot automatically triggers this method.
     */
    @Transactional
    // Ensures the database update is handled safely
    @CacheEvict(value = "userProfiles", key = "#event.userId")
    @KafkaListener(topics = "problem-solved-topic", groupId = "user-service-group")
    public void consumeProblemSolvedEvent(ProblemSolvedEvent event) {
        log.info("Received ProblemSolvedEvent for user: {} and problem: {}", event.getUserId(), event.getProblemId());

        try {
            // 1. Convert the String ID from the event back into a UUID
            UUID userUuid = UUID.fromString(event.getUserId());

            // 2. Fetch the user from PostgreSQL
            User user = userRepository.findById(userUuid)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userUuid));

            // 3. Increment the correct counter based on the difficulty string
            if (event.getDifficulty() != null) {
                switch (event.getDifficulty().toUpperCase()) {
                    case "EASY" -> user.setEasySolved(user.getEasySolved() + 1);
                    case "MEDIUM" -> user.setMediumSolved(user.getMediumSolved() + 1);
                    case "HARD" -> user.setHardSolved(user.getHardSolved() + 1);
                    default -> log.warn("Unknown difficulty level received in event: {}", event.getDifficulty());
                }

                // 4. Save the updated user back to the database
                userRepository.save(user);

                log.info("Successfully updated solved counts for user {}. Easy: {}, Medium: {}, Hard: {}",
                        user.getUsername(), user.getEasySolved(), user.getMediumSolved(), user.getHardSolved());
            }

        } catch (IllegalArgumentException e) {
            log.error("Invalid UUID format received from Kafka event: {}", event.getUserId(), e);
        } catch (Exception e) {
            log.error("Error processing ProblemSolvedEvent for user: {}", event.getUserId(), e);
        }
    }
}