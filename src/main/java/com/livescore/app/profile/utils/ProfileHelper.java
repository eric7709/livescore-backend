package com.livescore.app.profile.utils;

import org.springframework.stereotype.Component;

import com.livescore.app.auth.enums.Role;
import com.livescore.app.exceptions.BadRequestException;
import com.livescore.app.league.League;
import com.livescore.app.league.LeagueRepository;
import com.livescore.app.league.services.LeagueAccessService;
import com.livescore.app.profile.Profile;
import com.livescore.app.profile.ProfileRepository;
import com.livescore.app.profile.dto.ProfileRequestDTO;
import com.livescore.app.team.Team;
import com.livescore.app.team.TeamRepository;
import com.livescore.app.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/**
 * Shared lookups, validation, and entity mapping used by the individual
 * profile command/query services (CreateProfile, UpdateProfile, MakeCaptain,
 * etc.) so that logic isn't duplicated across each one.
 *
 * <p>Every profile carries a direct {@code league} reference. When a profile
 * is assigned to a team, its league is always taken from that team; otherwise
 * it comes from the explicit {@code leagueId} in the request.
 */
@Component
@RequiredArgsConstructor
public class ProfileHelper {

    private final ProfileRepository profileRepository;
    private final TeamRepository teamRepository;
    private final LeagueRepository leagueRepository;
    private final LeagueAccessService leagueAccessService;

    public Profile resolveProfile(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() ->
                        new BadRequestException(
                                "Profile not found with ID: " + id));
    }

    public Team resolveTeam(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() ->
                        new BadRequestException(
                                "Team not found with ID: " + id));
    }

    /**
     * Maps the request fields onto the entity, resolves the league (from the
     * team if one is given, otherwise from the explicit leagueId), validates
     * access to it, and sets it on the profile. Returns the resolved league,
     * or null if neither a team nor a leagueId was provided.
     */
    public League mapToEntity(Profile profile, ProfileRequestDTO dto) {
        profile.setFirstName(dto.getFirstName());
        profile.setLastName(dto.getLastName());
        profile.setPhoneNumber(dto.getPhoneNumber());
        profile.setAvatarUrl(dto.getAvatarUrl());

        profile.setRole(dto.getRole());
        profile.setPosition(dto.getPosition());
        profile.setStatus(dto.getStatus());
        profile.setSquadNumber(dto.getSquadNumber());

        profile.setPreferredFoot(dto.getPreferredFoot());
        profile.setHeight(dto.getHeight());
        profile.setDateOfBirth(dto.getDateOfBirth());

        League league;

        if (dto.getTeamId() != null) {
            Team team = resolveTeam(dto.getTeamId());
            league = team.getLeague();
            profile.setTeam(team);
        } else {
            profile.setTeam(null);
            league = dto.getLeagueId() == null
                    ? null
                    : leagueRepository.findById(dto.getLeagueId())
                            .orElseThrow(() -> new BadRequestException("League not found"));
        }

        if (league != null) {
            leagueAccessService.requireLeague(league);
        }

        profile.setLeague(league);
        return league;
    }

    /**
     * Resolves the league a profile belongs to, for access checks.
     */
    public League resolveLeague(Profile profile) {
        if (profile.getLeague() == null) {
            throw new BadRequestException(
                    "Profile " + profile.getId() + " is not associated with any league");
        }

        return profile.getLeague();
    }

    public void assignManagerIfNecessary(Profile profile) {

        if (profile.getRole() != Role.MANAGER || profile.getTeam() == null) {
            return;
        }

        Team team = profile.getTeam();
        team.setManager(profile);
        teamRepository.save(team);
    }

    public void validate(ProfileRequestDTO dto) {

        ValidationUtils.requireNonBlank(dto.getFirstName(), "First name is required");
        ValidationUtils.requireNonBlank(dto.getLastName(), "Last name is required");
        ValidationUtils.requireNonBlank(dto.getPhoneNumber(), "Phone number is required");

        ValidationUtils.requireNonNullObject(dto.getRole(), "Role is required");

        if (dto.getRole() == Role.PLAYER) {

            ValidationUtils.requireNonNullObject(
                    dto.getPosition(),
                    "Position is required");

            ValidationUtils.requireNonNullObject(
                    dto.getSquadNumber(),
                    "Squad number is required");

            ValidationUtils.requireNonNullObject(
                    dto.getStatus(),
                    "Player status is required");
        }
    }

    public void requireUniquePhone(String phoneNumber) {

        if (profileRepository.existsByPhoneNumber(phoneNumber)) {
            throw new BadRequestException(
                    "A profile with phone number " + phoneNumber + " already exists");
        }
    }

    public void requirePlayer(Profile profile) {

        if (profile.getRole() != Role.PLAYER) {
            throw new BadRequestException("Only players can have this operation applied");
        }
    }

    public void requireTeam(Profile profile) {

        if (profile.getTeam() == null) {
            throw new BadRequestException("Player must belong to a team");
        }
    }
}