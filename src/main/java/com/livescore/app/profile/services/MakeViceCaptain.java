package com.livescore.app.profile.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.exceptions.BadRequestException;
import com.livescore.app.profile.Profile;
import com.livescore.app.league.services.LeagueAccessService;
import com.livescore.app.profile.ProfileRepository;
import com.livescore.app.profile.enums.CaptainStatus;
import com.livescore.app.profile.utils.ProfileHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MakeViceCaptain {

    private final ProfileRepository profileRepository;
    private final ProfileHelper helper;
    private final LeagueAccessService leagueAccessService;

    @Transactional
    public Profile make(Long id) {

        Profile profile = helper.resolveProfile(id);
        leagueAccessService.requireLeagueRole(helper.resolveLeague(profile), com.livescore.app.auth.enums.Role.LEAGUE_OWNER, com.livescore.app.auth.enums.Role.LEAGUE_ADMIN);
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
