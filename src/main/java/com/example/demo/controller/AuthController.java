package com.example.demo.controller;

import com.example.demo.DTO.request.UserRegistrationDto;
import com.example.demo.entity.User;
import com.example.demo.services.UserServices;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserServices userServices;

    // Changed to /auth/api/register for REST API
    @PostMapping("/api/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserRegistrationDto userRegistrationDto) {
        try {
            if (userServices.existsByUsername(userRegistrationDto.getUsername())) {
                return ResponseEntity.badRequest().body("Username already exists");
            }
            if (userServices.existsByEmail(userRegistrationDto.getEmail())) {
                return ResponseEntity.badRequest().body("Email already exists");
            }

            User user = userServices.createUser(
                    userRegistrationDto.getUsername(),
                    userRegistrationDto.getPassword(),
                    userRegistrationDto.getEmail(),
                    userRegistrationDto.getFirstName(),
                    userRegistrationDto.getLastName()

            );
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("User registered successfully");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login() {
        return ResponseEntity.ok("Login successful");
    }
}