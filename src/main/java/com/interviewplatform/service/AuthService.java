package com.interviewplatform.service;

import com.interviewplatform.dto.AuthResponse;
import com.interviewplatform.dto.LoginRequest;
import com.interviewplatform.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
