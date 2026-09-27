package com.zestio.app.profile.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zestio.app.exceptions.BadRequestException;
import com.zestio.app.profile.Profile;
import com.zestio.app.profile.ProfileRepository;
import com.zestio.app.profile.enums.CaptainStatus;
import com.zestio.app.profile.helpers.ProfileHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MakeViceCaptain {

    private final ProfileRepository profileRepository;
    private final ProfileHelper helper;

    @Transactional
    public Profile make(Long id) {

        Profile profile = helper.resolveProfile(id);
        helper.requirePlayer(profile);
        helper.requireTeam(profile);

        if (profile.getCaptainStatus() == CaptainStatus.CAPTAIN) {
            throw new BadRequestException("Player is already the captain");
        }

        if (profile.getCaptainStatus() == CaptainStatus.VICE_CAPTAIN) {
            return profile;
        }

        profileRepository.findByTeamIdAndCaptainStatus(profile.getTeam().getId(), CaptainStatus.VICE_CAPTAIN)
                .filter(current -> !current.getId().equals(profile.getId()))
                .ifPresent(current -> {
                    current.setCaptainStatus(CaptainStatus.NONE);
                    profileRepository.save(current);
                });

        profile.setCaptainStatus(CaptainStatus.VICE_CAPTAIN);

        return profileRepository.save(profile);
    }
}
