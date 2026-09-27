package com.zestio.app.auth.dto;

import com.zestio.app.profile.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateInviteRequest {
    @NotNull
    private Role role;

    private Long teamId; // optional — omit for ADMIN/MODERATOR invites

    private Integer expiresInDays; // optional, defaults to 7
}