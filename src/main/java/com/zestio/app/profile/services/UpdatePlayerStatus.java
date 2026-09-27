package com.zestio.app.profile.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zestio.app.profile.Profile;
import com.zestio.app.profile.ProfileRepository;
import com.zestio.app.profile.enums.PlayerStatus;
import com.zestio.app.profile.helpers.ProfileHelper;
import com.zestio.app.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdatePlayerStatus {

    private final ProfileRepository profileRepository;
    private final ProfileHelper helper;

    @Transactional
    public Profile update(Long id, PlayerStatus status) {

        ValidationUtils.requireNonNullObject(status, "Status is required");

        Profile profile = helper.resolveProfile(id);
        helper.requirePlayer(profile);

        profile.setStatus(status);

        return profileRepository.save(profile);
    }
}
