package com.example.backend.dto;

import com.example.backend.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Table(name = "user_devices")
@Data
public class UserDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime loginDate;
    @ManyToOne
    private UserEntity user;
}