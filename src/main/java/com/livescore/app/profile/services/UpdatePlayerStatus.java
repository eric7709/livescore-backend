package com.livescore.app.profile.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.profile.Profile;
import com.livescore.app.league.services.LeagueAccessService;
import com.livescore.app.profile.ProfileRepository;
import com.livescore.app.profile.enums.PlayerStatus;
import com.livescore.app.profile.utils.ProfileHelper;
import com.livescore.app.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdatePlayerStatus {

    private final ProfileRepository profileRepository;
    private final ProfileHelper helper;
    private final LeagueAccessService leagueAccessService;

    @Transactional
    public Profile update(Long id, PlayerStatus status) {

        ValidationUtils.requireNonNullObject(status, "Status is required");

        Profile profile = helper.resolveProfile(id);
        leagueAccessService.requireLeagueRole(helper.resolveLeague(profile), com.livescore.app.auth.enums.Role.LEAGUE_OWNER, com.livescore.app.auth.enums.Role.LEAGUE_ADMIN);
        helper.requirePlayer(profile);

        profile.setStatus(status);

        return profileRepository.save(profile);
    }
}
