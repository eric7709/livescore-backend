package com.livescore.app.profile.services;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.livescore.app.auth.enums.Role;
import com.livescore.app.exceptions.BadRequestException;
import com.livescore.app.league.League;
import com.livescore.app.league.services.LeagueAccessService;
import com.livescore.app.profile.Profile;
import com.livescore.app.profile.ProfileRepository;
import com.livescore.app.profile.dto.ProfileRequestDTO;
import com.livescore.app.profile.utils.ProfileHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateProfile {

    private final ProfileRepository profileRepository;
    private final ProfileHelper helper;
    private final LeagueAccessService leagueAccessService;

    @Transactional
    public Profile create(ProfileRequestDTO dto) {
        helper.validate(dto);
        leagueAccessService.requireAnyRole(Role.ADMIN, Role.LEAGUE_OWNER, Role.LEAGUE_ADMIN);
        helper.requireUniquePhone(dto.getPhoneNumber());

        Profile profile = new Profile();
        League league = helper.mapToEntity(profile, dto);

        if (league == null) {
            throw new BadRequestException("leagueId or teamId is required for a profile");
        }

        requireUniqueSquadNumber(profile);

        Profile saved = profileRepository.save(profile);
        helper.assignManagerIfNecessary(saved);

        return saved;
    }

    private void requireUniqueSquadNumber(Profile profile) {
        if (profile.getTeam() == null || profile.getSquadNumber() == null) return;

        if (profileRepository.existsByTeamAndSquadNumber(profile.getTeam(), profile.getSquadNumber())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Squad number " + profile.getSquadNumber() + " is already taken at " + profile.getTeam().getName()
            );
        }
    }
}