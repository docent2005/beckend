package com.example.backend.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;
    private String twoFactorPassword;
}
