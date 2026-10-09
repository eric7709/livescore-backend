package com.livescore.app.team.dto;

import com.livescore.app.profile.Profile;
import com.livescore.app.profile.enums.CaptainStatus;
import com.livescore.app.profile.enums.PlayerStatus;
import com.livescore.app.profile.enums.Position;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Player {
    private String id;
    private String fullName;
    private Integer squadNumber;
    private String avatarUrl;
    private PlayerStatus status;
    private Position position;
    private CaptainStatus captainStatus;
    
    public static Player toPlayer(Profile profile) {
        return Player.builder()
                .id(profile.getId().toString())
                .fullName(profile.getFullName())
                .squadNumber(profile.getSquadNumber())
                .avatarUrl(profile.getAvatarUrl())
                .position(profile.getPosition())
                .status(profile.getStatus())
                .captainStatus(profile.getCaptainStatus())
                .build();
    }
}