package com.zestio.app.competition.dto;

import lombok.Data;

@Data
public class PlayerStatDTO {
    private Long playerId;
    private String name;
    private Long teamId;
    private String teamName;
    private String teamLogoUrl;
    private int numberOfGoals;
    private int numberOfAssists;
}