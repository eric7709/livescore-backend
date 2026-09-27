package com.zestio.app.profile.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.zestio.app.profile.Profile;
import com.zestio.app.profile.ProfileRepository;
import com.zestio.app.profile.dto.ProfileRequestDTO;
import com.zestio.app.profile.helpers.ProfileHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateProfile {

    private final ProfileRepository profileRepository;
    private final ProfileHelper helper;

    @Transactional
    public Profile create(ProfileRequestDTO dto) {
        helper.validate(dto);
        helper.requireUniquePhone(dto.getPhoneNumber());

        Profile profile = new Profile();
        helper.mapToEntity(profile, dto);

        requireUniqueSquadNumber(profile, null);

        Profile saved = profileRepository.save(profile);
        helper.assignManagerIfNecessary(saved);

        return saved;
    }

    private void requireUniqueSquadNumber(Profile profile, Long excludeId) {
        if (profile.getTeam() == null || profile.getSquadNumber() == null) return;

        boolean taken = excludeId == null
                ? profileRepository.existsByTeamAndSquadNumber(profile.getTeam(), profile.getSquadNumber())
                : profileRepository.existsByTeamAndSquadNumberAndIdNot(profile.getTeam(), profile.getSquadNumber(), excludeId);

        if (taken) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Squad number " + profile.getSquadNumber() + " is already taken at " + profile.getTeam().getName()
            );
        }
    }
}