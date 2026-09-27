package com.zestio.app.profile.helpers;

import org.springframework.stereotype.Component;

import com.zestio.app.exceptions.BadRequestException;
import com.zestio.app.profile.Profile;
import com.zestio.app.profile.ProfileRepository;
import com.zestio.app.profile.dto.ProfileRequestDTO;
import com.zestio.app.profile.enums.Role;
import com.zestio.app.team.Team;
import com.zestio.app.team.TeamRepository;
import com.zestio.app.utils.ValidationUtils;

import lombok.RequiredArgsConstructor;

/**
 * Shared lookups, validation, and entity mapping used by the individual
 * profile command/query services (CreateProfile, UpdateProfile, MakeCaptain,
 * etc.) so that logic isn't duplicated across each one.
 */
@Component
@RequiredArgsConstructor
public class ProfileHelper {

    private final ProfileRepository profileRepository;
    private final TeamRepository teamRepository;

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

    public void mapToEntity(Profile profile, ProfileRequestDTO dto) {
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

        if (dto.getTeamId() != null) {
            Team team = resolveTeam(dto.getTeamId());
            profile.setTeam(team);
        } else {
            profile.setTeam(null);
        }
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
