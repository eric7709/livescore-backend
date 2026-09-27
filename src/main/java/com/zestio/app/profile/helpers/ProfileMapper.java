package com.zestio.app.profile.helpers;

import java.util.List;
import org.springframework.stereotype.Component;

import com.zestio.app.profile.Profile;
import com.zestio.app.profile.dto.ProfileResponseDTO;
import com.zestio.app.team.Team;

@Component
public class ProfileMapper {
    /**
     * Maps a profile to ProfileResponseDTO.
     * Defaults isStarter to false.
     */
    public ProfileResponseDTO toDTO(Profile profile) {
        return toDTO(profile, false);
    }

    /**
     * Maps a profile to ProfileResponseDTO with lineup context.
     * Used when the player's starter/substitute status is known.
     */
    public ProfileResponseDTO toDTO(Profile profile, boolean isStarter) {

        if (profile == null) {
            return null;
        }

        Team team = profile.getTeam();

        return ProfileResponseDTO.builder()
                .id(profile.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .fullName(profile.getFullName())
                .phoneNumber(profile.getPhoneNumber())
                .avatarUrl(profile.getAvatarUrl())
                .squadNumber(profile.getSquadNumber())
                .status(profile.getStatus())
                .role(profile.getRole())
                .position(profile.getPosition())
                .captainStatus(profile.getCaptainStatus())
                .preferredFoot(profile.getPreferredFoot())
                .height(profile.getHeight())
                .dateOfBirth(profile.getDateOfBirth())
                .isStarter(isStarter)
                .teamId(team != null ? team.getId() : null)
                .teamName(team != null ? team.getName() : null)
                .build();
    }

    /**
     * Maps a list of profiles.
     * Generic profile lists default isStarter to false.
     */
    public List<ProfileResponseDTO> toListDTO(List<Profile> profiles) {

        if (profiles == null || profiles.isEmpty()) {
            return List.of();
        }

        return profiles.stream()
                .map(this::toDTO)
                .toList();
    }
}