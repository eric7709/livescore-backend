package com.zestio.app.auth.dto;

import com.zestio.app.profile.Profile;
import com.zestio.app.profile.enums.CaptainStatus;
import com.zestio.app.profile.enums.PlayerStatus;
import com.zestio.app.profile.enums.Position;
import com.zestio.app.profile.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProfileDetailsResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private Integer squadNumber;
    private PlayerStatus status;
    private Role role;
    private Position position;
    private CaptainStatus captainStatus;
    private Long teamId;

    public static ProfileDetailsResponse toDTO(Profile profile) {
        return new ProfileDetailsResponse(
                profile.getId(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getFullName(),
                profile.getPhoneNumber(),
                profile.getAvatarUrl(),
                profile.getSquadNumber(),
                profile.getStatus(),
                profile.getRole(),
                profile.getPosition(),
                profile.getCaptainStatus(),
                profile.getTeam() != null ? profile.getTeam().getId() : null
        );
    }
}