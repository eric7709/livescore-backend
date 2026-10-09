package com.livescore.app.profile.dto;

import java.time.LocalDate;

import com.livescore.app.auth.enums.Role;
import com.livescore.app.profile.enums.PlayerStatus;
import com.livescore.app.profile.enums.Position;
import com.livescore.app.profile.enums.PreferredFoot;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileRequestDTO {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String avatarUrl;
    private PlayerStatus status;
    private Integer squadNumber;
    private Long teamId;
    private Long leagueId;
    private Role role;
    private Position position;
    private PreferredFoot preferredFoot;
    private Integer height;
    private LocalDate dateOfBirth;
}