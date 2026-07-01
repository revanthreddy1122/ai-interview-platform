package com.interviewplatform.service;

import com.interviewplatform.dto.ChangePasswordRequest;
import com.interviewplatform.dto.ProfileResponse;
import com.interviewplatform.dto.ProfileUpdateRequest;

public interface ProfileService {

    ProfileResponse getProfile();

    ProfileResponse updateProfile(ProfileUpdateRequest request);

    void changePassword(ChangePasswordRequest request);
}
