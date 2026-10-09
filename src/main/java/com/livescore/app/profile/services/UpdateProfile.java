package com.livescore.app.profile.services;

import java.util.Objects;

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
public class UpdateProfile {

    private final ProfileRepository profileRepository;
    private final ProfileHelper helper;
    private final LeagueAccessService leagueAccessService;

    @Transactional
    public Profile update(Long id, ProfileRequestDTO dto) {

        Profile profile = helper.resolveProfile(id);
        leagueAccessService.requireLeagueRole(
                helper.resolveLeague(profile),
                Role.LEAGUE_OWNER,
                Role.LEAGUE_ADMIN);

        helper.validate(dto);

        if (!Objects.equals(profile.getPhoneNumber(), dto.getPhoneNumber())) {
            helper.requireUniquePhone(dto.getPhoneNumber());
        }

        League league = helper.mapToEntity(profile, dto);

        if (league == null) {
            throw new BadRequestException("leagueId or teamId is required for a profile");
        }

        requireUniqueSquadNumber(profile, id);

        Profile saved = profileRepository.save(profile);
        helper.assignManagerIfNecessary(saved);

        return saved;
    }

    private void requireUniqueSquadNumber(Profile profile, Long excludeId) {
        if (profile.getTeam() == null || profile.getSquadNumber() == null) return;

        if (profileRepository.existsByTeamAndSquadNumberAndIdNot(profile.getTeam(), profile.getSquadNumber(), excludeId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Squad number " + profile.getSquadNumber() + " is already taken at " + profile.getTeam().getName()
            );
        }
    }
}