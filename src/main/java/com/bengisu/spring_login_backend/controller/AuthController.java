package com.bengisu.spring_login_backend.controller;

import com.bengisu.spring_login_backend.model.User;
import com.bengisu.spring_login_backend.repository.UserRepository;
import com.bengisu.spring_login_backend.security.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository userRepository, JwtService jwtService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    record RegisterRequest(String email, String password) {
    }

    record ErrorResponse(String error) {
    }

    record UserResponse(Long id, String email) {
    }

    record LoginRequest(String email, String password) {
    }

    record TokenResponse(String token) {
    }

    @PostMapping("/api/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.email() == null || request.email().isBlank() || request.password() == null
                || request.password().isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Email ve şifre zorunlu"));
        }

        if (request.password().length() < 6) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Şifre en az 6 karakter olmalı"));
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("Bu email zaten kayıtlı"));
        }

        User user = new User();
        user.setEmail(request.email());

        user.setPasswordHash(passwordEncoder.encode(request.password()));

        User saved = userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(new UserResponse(saved.getId(), saved.getEmail()));
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        if (request.email() == null || request.email().isBlank() || request.password() == null
                || request.password().isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Email ve şifre zorunlu"));
        }

        var userOpt = userRepository.findByEmail(request.email());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Email veya şifre hatalı"));
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Email veya şifre hatalı"));
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return ResponseEntity.ok(new TokenResponse(token));

    }

}
