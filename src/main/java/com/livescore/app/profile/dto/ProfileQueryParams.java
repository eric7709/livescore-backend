package com.livescore.app.profile.dto;

import java.time.LocalDate;

import com.livescore.app.auth.enums.Role;
import com.livescore.app.profile.enums.CaptainStatus;
import com.livescore.app.profile.enums.PlayerStatus;
import com.livescore.app.profile.enums.Position;
import com.livescore.app.profile.enums.PreferredFoot;

import lombok.Data;

@Data
public class ProfileQueryParams {

    private Long leagueId;

    private Long teamId;

    private String firstName;

    private String lastName;

    private String fullName;

    private String phoneNumber;

    private Integer squadNumber;

    private PlayerStatus status;

    private Role role;

    private Position position;

    private CaptainStatus captainStatus;

    private PreferredFoot preferredFoot;

    private Integer minHeight;

    private Integer maxHeight;

    private LocalDate dateOfBirthFrom;

    private LocalDate dateOfBirthTo;
}