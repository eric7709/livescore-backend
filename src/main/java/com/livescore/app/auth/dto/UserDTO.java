package com.livescore.app.auth.dto;

import com.livescore.app.auth.User;
import com.livescore.app.auth.enums.Role;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private Role role;
    private String email;
    private Long leagueId;
    private String leagueName;
    private boolean enabled;
    private Long profileId;

    public static UserDTO fromEntity(User user) {

        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .email(user.getEmail())
                .leagueId(user.getLeague() != null ? user.getLeague().getId() : null)
                .leagueName(user.getLeague() != null ? user.getLeague().getName() : null)
                .enabled(user.isEnabled())
                .profileId(
                        user.getProfile() != null
                                ? user.getProfile().getId()
                                : null)
                .build();
    }

}