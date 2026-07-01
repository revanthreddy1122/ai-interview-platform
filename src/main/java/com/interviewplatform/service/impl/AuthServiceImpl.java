package com.interviewplatform.service.impl;

import com.interviewplatform.dto.AuthResponse;
import com.interviewplatform.dto.LoginRequest;
import com.interviewplatform.dto.RegisterRequest;
import com.interviewplatform.entity.DashboardMetrics;
import com.interviewplatform.entity.Role;
import com.interviewplatform.entity.User;
import com.interviewplatform.exception.DuplicateResourceException;
import com.interviewplatform.exception.InvalidCredentialsException;
import com.interviewplatform.repository.DashboardMetricsRepository;
import com.interviewplatform.repository.UserRepository;
import com.interviewplatform.security.JwtUtil;
import com.interviewplatform.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final DashboardMetricsRepository dashboardMetricsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .role(Role.USER)
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);

        DashboardMetrics metrics = DashboardMetrics.builder()
                .user(savedUser)
                .resumeCount(0)
                .totalInterviewsAttempted(0)
                .lastUpdated(LocalDateTime.now())
                .build();
        dashboardMetricsRepository.save(metrics);

        String token = jwtUtil.generateToken(savedUser, savedUser.getUserId());

        log.info("New user registered: {}", savedUser.getEmail());

        return AuthResponse.builder()
                .userId(savedUser.getUserId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationMs() / 1000)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase().trim(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        String token = jwtUtil.generateToken(user, user.getUserId());

        log.info("User logged in: {}", user.getEmail());

        return AuthResponse.builder()
                .userId(user.getUserId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtUtil.getExpirationMs() / 1000)
                .build();
    }
}
