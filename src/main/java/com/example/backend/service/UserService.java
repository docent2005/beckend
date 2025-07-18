package com.example.backend.service;

import com.example.backend.dto.LoginResponse;
import com.example.backend.dto.UserDevice;
import com.example.backend.dto.UserDeviceRepository;
import com.example.backend.dto.UserProfile;
import com.example.backend.entity.UserEntity;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service // Позначає, що цей клас є "сервісом" і керується Spring
@RequiredArgsConstructor // Автоматично створює конструктор для final-полів (через Lombok)
public class UserService {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDeviceRepository userDeviceRepository;

    public UserEntity register(String email, String password, String twoFactorPassword, String ipAddress) {
        UserEntity user = UserEntity.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .twoFactorPasswordHash(passwordEncoder.encode(twoFactorPassword))
                .createdAt(LocalDateTime.now())
                .enabled(true)
                .build();

        user = userRepository.save(user);

        UserDevice device = new UserDevice();
        device.setUser(user);
        device.setIpAddress(ipAddress);
        userDeviceRepository.save(device);

        return user;
    }

    // Метод для пошуку користувача за email
    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    public LoginResponse login(String email, String password, String ip) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow();
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Невірний пароль");
        }

        boolean knownDevice = userDeviceRepository.existsByUserAndIpAddress(user, ip);
        String token = jwtUtil.generateToken(user.getEmail());
        if (!knownDevice) {
            return new LoginResponse("Необхідна 2FA", true, token);
        }

        return new LoginResponse("Вхід успішний", false, token);
    }

    public UserProfile getUserProfile(String email) {
        Optional<UserEntity> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            return null;
        }
        return new UserProfile(
                user.get().getEmail(),
                user.get().getCreatedAt(),
                user.get().getLastLogin(),
                "Другий пароль" // або витягнути відкритий варіант
        );
    }
}
