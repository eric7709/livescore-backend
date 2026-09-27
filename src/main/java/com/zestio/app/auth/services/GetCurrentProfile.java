package com.zestio.app.auth.services;

import com.zestio.app.profile.Profile;
import com.zestio.app.profile.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetCurrentProfile {

    private final ProfileRepository profileRepository;

    public Profile get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String phoneNumber = authentication.getName(); // set from JWT subject in JwtAuthenticationFilter

        return profileRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalStateException("Authenticated profile not found"));
    }
}