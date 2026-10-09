package com.livescore.app.team.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamRequestDTO {
    private String name;
    private Long leagueId;
    private Long managerId;
    private String logoUrl;
    private String teamCode;
    private String stadium;
}