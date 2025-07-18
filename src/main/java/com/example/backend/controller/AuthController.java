package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.entity.UserEntity;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtUtil;
import com.example.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserDeviceRepository userDeviceRepository;
    private final UserRepository userRepository;

    // Реєстрація
    @PostMapping("/register")
    public String register(@RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        if (userService.findByEmail(request.getEmail()) != null) {
            return "користувач вже існує!";
        }

        String ipAddress = httpRequest.getRemoteAddr();
        System.out.println("IP: " + ipAddress);
        String ip = httpRequest.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = httpRequest.getRemoteAddr();
        }
        userService.register(
                request.getEmail(),
                request.getPassword(),
                request.getTwoFactorPassword(),
                ipAddress
        );
        return "реєстрація успішна!";
    }


    @PostMapping("/login")
    public LoginResponse login(@RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        UserEntity user = userService.findByEmail(request.getEmail());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("невірний email або пароль!");
        }

        // Зчитуємо IP
        String ip = httpRequest.getHeader("X-Forwarded-For");
        if (ip == null)
            ip = httpRequest.getRemoteAddr();
        System.out.println("IP користувача: " + ip);

        boolean known = userDeviceRepository.existsByUserAndIpAddress(user, ip);
        String token = jwtUtil.generateToken(user.getEmail());
        // Якщо IP новий — вимагаємо другий пароль
        if (!known) {
            return new LoginResponse("Необхідне підтвердження 2FA", true, token);
        }

        // IP відомий — видаємо токен
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        return new LoginResponse("Вхід успішний", false, token);
    }

    @PostMapping("/verify-2fa")
    public AuthResponse verify2FA(
            @RequestBody AuthRequest request,
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest) {

        String token = authHeader.substring(7); // Видаляємо "Bearer "
        String email = jwtUtil.extractEmail(token);
        UserEntity user = userService.findByEmail(email);

        if (user == null || !passwordEncoder.matches(request.getTwoFactorPassword(), user.getTwoFactorPasswordHash())) {
            throw new RuntimeException("Невірний другий пароль");
        }

        // Отримуємо IP-адресу
        String ip = httpRequest.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = httpRequest.getRemoteAddr();
        }

        System.out.println("IP, який додаємо до відомих (якщо новий): " + ip);

        // Перевіряємо, чи IP вже збережений
        boolean alreadyKnown = userDeviceRepository.existsByUserAndIpAddress(user, ip);
        if (!alreadyKnown) {
            UserDevice device = new UserDevice();
            device.setUser(user);
            device.setIpAddress(ip);
            userDeviceRepository.save(device);
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String newToken = jwtUtil.generateToken(user.getEmail());
        return new AuthResponse(newToken);
    }

}