package com.livescore.app.profile.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.auth.enums.Role;
import com.livescore.app.profile.ProfileRepository;
import com.livescore.app.profile.dto.ProfileStatsDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetProfileStats {

    private final ProfileRepository profileRepository;

    public ProfileStatsDTO get() {
        return new ProfileStatsDTO(
                profileRepository.count(),
                profileRepository.countByRole(Role.PLAYER),
                profileRepository.countByRole(Role.STAFF),
                profileRepository.countByRole(Role.MANAGER));
    }
}
