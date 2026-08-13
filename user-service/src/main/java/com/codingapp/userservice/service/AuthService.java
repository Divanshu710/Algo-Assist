package com.codingapp.userservice.service;

import com.codingapp.userservice.dto.AuthResponse;
import com.codingapp.userservice.dto.LoginRequest;
import com.codingapp.userservice.dto.RegisterRequest;
import com.codingapp.userservice.exception.InvalidCredentialsException;
import com.codingapp.userservice.exception.UserAlreadyExistsException;
import com.codingapp.userservice.model.User;
import com.codingapp.userservice.model.UserTier;
import com.codingapp.userservice.repository.UserRepository;
import com.codingapp.userservice.security.JwtUtils;
import com.codingapp.userservice.security.PasswordConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;


    public AuthResponse registrationHandler(RegisterRequest registerRequest){

        // check for the username
        if(userRepository.existsByUsername(registerRequest.getUsername())){
            throw new UserAlreadyExistsException("Username is already taken.");
        }
        // check for the email
        if(userRepository.existsByEmail(registerRequest.getEmail())){
            throw new UserAlreadyExistsException("Email is already registered.");
        }

        String password = registerRequest.getPassword();
        String hashedPassword = passwordEncoder.encode(password);

        // create a user entity
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .passwordHash(hashedPassword)
                .userTier(UserTier.FREE)
                .build();

        User savedUser = userRepository.save(user);

        String token = jwtUtils.generateToken(savedUser.getUserId(), savedUser.getUsername(), savedUser.getUserTier());

        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getUserId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .userTier(savedUser.getUserTier())
                .build();
    }

    public AuthResponse loginHandler(LoginRequest loginRequest){

        // check for the email
        User loginUser = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(()-> new InvalidCredentialsException("Invalid email or password."));


        String hashedPassword = loginUser.getPasswordHash();
        if(!passwordEncoder.matches(loginRequest.getPassword(), hashedPassword)){
            throw new InvalidCredentialsException("Invalid email or Password");
        }

        String token = jwtUtils.generateToken(loginUser.getUserId(), loginUser.getUsername(), loginUser.getUserTier());

        return AuthResponse.builder()
                .token(token)
                .userId(loginUser.getUserId())
                .username(loginUser.getUsername())
                .email(loginUser.getEmail())
                .userTier(loginUser.getUserTier())
                .build();

    }
}
