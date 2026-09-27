package com.zestio.app.auth.dto;

import com.zestio.app.auth.InviteCode;
import com.zestio.app.profile.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class InviteResponse {
    private String code;
    private Role role;
    private Long teamId;
    private Instant expiresAt;

    public static InviteResponse toDTO(InviteCode invite) {
        return new InviteResponse(
                invite.getCode(),
                invite.getRole(),
                invite.getTeam() != null ? invite.getTeam().getId() : null,
                invite.getExpiryDate()
        );
    }
}