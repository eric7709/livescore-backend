package com.livescore.app.profile.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.profile.Profile;
import com.livescore.app.league.services.LeagueAccessService;
import com.livescore.app.profile.ProfileRepository;
import com.livescore.app.profile.enums.CaptainStatus;
import com.livescore.app.profile.utils.ProfileHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MakeCaptain {

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
            return profile;
        }

        profileRepository.findByTeamIdAndCaptainStatus(profile.getTeam().getId(), CaptainStatus.CAPTAIN)
                .filter(current -> !current.getId().equals(profile.getId()))
                .ifPresent(current -> {
                    current.setCaptainStatus(CaptainStatus.NONE);
                    profileRepository.save(current);
                });

        profile.setCaptainStatus(CaptainStatus.CAPTAIN);

        return profileRepository.save(profile);
    }
}
