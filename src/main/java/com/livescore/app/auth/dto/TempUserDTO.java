package com.livescore.app.auth.dto;

import com.livescore.app.auth.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TempUserDTO {

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @NotNull
    private Role role;

    private Long leagueId;

    /** Required when role is MANAGER (checked in CreateInviteLink). */
    private Long teamId;
}