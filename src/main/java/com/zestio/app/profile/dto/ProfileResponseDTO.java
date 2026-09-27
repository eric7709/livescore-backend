package com.zestio.app.profile.dto;

import java.time.LocalDate;

import com.zestio.app.profile.enums.PlayerStatus;
import com.zestio.app.profile.enums.CaptainStatus;
import com.zestio.app.profile.enums.Role;
import com.zestio.app.profile.enums.Position;
import com.zestio.app.profile.enums.PreferredFoot;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ProfileResponseDTO {
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

    private PreferredFoot preferredFoot;

    private Integer height;

    private LocalDate dateOfBirth;

    private boolean isStarter;

    private Long teamId;

    private String teamName;
}