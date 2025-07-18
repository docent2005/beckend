package com.example.backend.controller;

import com.example.backend.dto.UserProfile;
import com.example.backend.security.JwtUtil;
import com.example.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserProfile> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // Видаляємо "Bearer "
        String email = jwtUtil.extractEmail(token);
        UserProfile userProfile = userService.getUserProfile(email);
        if (userProfile == null) {
            return ResponseEntity.status(404).build();
        }
        return ResponseEntity.ok(userProfile);
    }
}