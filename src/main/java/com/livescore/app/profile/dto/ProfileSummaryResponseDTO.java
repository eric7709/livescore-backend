package com.livescore.app.profile.dto;

import com.livescore.app.profile.enums.PlayerStatus;
import com.livescore.app.profile.enums.Position;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileSummaryResponseDTO {
    private Long id;
    private String fullName;
    private Integer squadNumber;
    private Position position;
    private String avatarUrl;
    private PlayerStatus status;
}