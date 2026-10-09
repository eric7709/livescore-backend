package com.livescore.app.auth.dto;

import java.time.LocalDateTime;

import com.livescore.app.auth.tempUserAndPasswordToken.TempUser;
import com.livescore.app.auth.enums.Role;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TempUserResponseDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private Long leagueId;
    private Role role;
    private LocalDateTime expiresAt;

    public static TempUserResponseDTO toDTO(TempUser tempUser) {
        if (tempUser == null) return null;

        TempUserResponseDTO dto = new TempUserResponseDTO();
        dto.setId(tempUser.getId());
        dto.setFirstName(tempUser.getFirstName());
        dto.setLastName(tempUser.getLastName());
        dto.setEmail(tempUser.getEmail());
        dto.setRole(tempUser.getRole());
        dto.setExpiresAt(tempUser.getExpiresAt());
        if (tempUser.getLeague() != null) {
            dto.setLeagueId(tempUser.getLeague().getId());
        }
        return dto;
    }
}