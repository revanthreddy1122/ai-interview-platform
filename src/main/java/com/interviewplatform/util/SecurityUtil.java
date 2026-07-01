package com.interviewplatform.util;

import com.interviewplatform.entity.User;
import com.interviewplatform.exception.UnauthorizedAccessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || !(authentication.getPrincipal() instanceof User)) {
            throw new UnauthorizedAccessException("No authenticated user found in security context");
        }
        return (User) authentication.getPrincipal();
    }

    public Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }
}
