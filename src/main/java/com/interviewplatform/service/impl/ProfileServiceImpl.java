package com.interviewplatform.service.impl;

import com.interviewplatform.dto.ChangePasswordRequest;
import com.interviewplatform.dto.ProfileResponse;
import com.interviewplatform.dto.ProfileUpdateRequest;
import com.interviewplatform.entity.User;
import com.interviewplatform.exception.InvalidCredentialsException;
import com.interviewplatform.repository.UserRepository;
import com.interviewplatform.service.ProfileService;
import com.interviewplatform.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtil securityUtil;

    @Override
    public ProfileResponse getProfile() {
        User user = securityUtil.getCurrentUser();
        return mapToDTO(user);
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(ProfileUpdateRequest request) {
        User user = securityUtil.getCurrentUser();
        User managedUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            managedUser.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            managedUser.setPhoneNumber(request.getPhoneNumber());
        }

        User savedUser = userRepository.save(managedUser);
        log.info("Profile updated for user {}", savedUser.getUserId());

        return mapToDTO(savedUser);
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        User user = securityUtil.getCurrentUser();
        User managedUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), managedUser.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        managedUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(managedUser);

        log.info("Password changed for user {}", managedUser.getUserId());
    }

    private ProfileResponse mapToDTO(User user) {
        return ProfileResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
