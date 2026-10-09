package com.livescore.app.profile.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.auth.enums.Role;
import com.livescore.app.league.services.LeagueAccessService;
import com.livescore.app.profile.Profile;
import com.livescore.app.profile.ProfileRepository;
import com.livescore.app.profile.utils.ProfileHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteProfile {

    private final ProfileRepository profileRepository;
    private final ProfileHelper helper;
    private final LeagueAccessService leagueAccessService;

    @Transactional
    public void delete(Long id) {
        Profile profile = helper.resolveProfile(id);
        leagueAccessService.requireLeagueRole(helper.resolveLeague(profile), Role.LEAGUE_OWNER, Role.LEAGUE_ADMIN);
        profileRepository.delete(profile);
    }
}
