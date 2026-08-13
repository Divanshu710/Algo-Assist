package com.codingapp.userservice.repository;

import com.codingapp.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Used during Login to find the user by email
    Optional<User> findByEmail(String email);

    // Used during Registration to check if email is already taken
    boolean existsByEmail(String email);

    // Used during Registration to check if username is already taken
    boolean existsByUsername(String username);
}