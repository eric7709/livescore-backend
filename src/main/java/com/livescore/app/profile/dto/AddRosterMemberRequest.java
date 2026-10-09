package com.livescore.app.profile.dto;

import java.time.LocalDate;

import com.livescore.app.auth.enums.Role;
import com.livescore.app.profile.enums.Position;
import com.livescore.app.profile.enums.PreferredFoot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddRosterMemberRequest {
    @NotBlank
    private String firstName;

    private PreferredFoot preferredFoot;

    @NotBlank
    private String lastName;

    @NotBlank
    private String phoneNumber;

    @NotNull
    private Role role; // PLAYER or STAFF only

    private Long teamId;

    private Position position; // relevant for PLAYER, ignore for STAFF

    private Integer squadNumber;

    private Integer height;

    private LocalDate dateOfBirth;
}