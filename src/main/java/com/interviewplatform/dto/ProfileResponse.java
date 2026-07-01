package com.interviewplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {

    private Long userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String role;
    private LocalDateTime createdAt;
}
