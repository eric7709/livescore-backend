package com.livescore.app.profile.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.profile.Profile;
import com.livescore.app.profile.ProfileRepository;
import com.livescore.app.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetProfilesByTeam {

    private final ProfileRepository profileRepository;

    public List<Profile> get(Long teamId) {
        ValidationUtils.requireNonNullObject(teamId, "Team ID cannot be null");
        return profileRepository.findByTeamId(teamId);
    }
}
