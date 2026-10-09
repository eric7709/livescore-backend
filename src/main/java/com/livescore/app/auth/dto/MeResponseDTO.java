package com.livescore.app.auth.dto;

import com.livescore.app.auth.User;
import com.livescore.app.auth.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeResponseDTO {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private Long profileId;
    private Long leagueId;
    private Long teamId;
    private String teamName;

    /** Single mapping used by both login and /me. Call inside a transaction (lazy fields). */
    public static MeResponseDTO fromEntity(User user) {
        var profile = user.getProfile();
        var league = user.getLeague();
        var team = profile != null ? profile.getTeam() : null;

        return new MeResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                profile != null ? profile.getId() : null,
                league != null ? league.getId() : null,
                team != null ? team.getId() : null,
                team != null ? team.getName() : null
        );
    }
}