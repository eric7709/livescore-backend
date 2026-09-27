package com.zestio.app.profile.dto;

import java.time.LocalDate;

import com.zestio.app.profile.enums.PlayerStatus;
import com.zestio.app.profile.enums.Position;
import com.zestio.app.profile.enums.PreferredFoot;
import com.zestio.app.profile.enums.Role;

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
    private Role role;
    private Position position;
    private PreferredFoot preferredFoot;
    private Integer height;
    private LocalDate dateOfBirth;
}