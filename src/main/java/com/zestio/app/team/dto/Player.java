package com.zestio.app.team.dto;

import com.zestio.app.profile.Profile;
import com.zestio.app.profile.enums.CaptainStatus;
import com.zestio.app.profile.enums.PlayerStatus;
import com.zestio.app.profile.enums.Position;

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