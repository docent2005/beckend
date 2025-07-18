package com.example.backend.dto;

import com.example.backend.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    boolean existsByUserAndIpAddress(UserEntity user, String ipAddress);
}