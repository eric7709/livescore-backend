package com.livescore.app.profile.dto;

import lombok.Data;

@Data 
public class StatsByCompetition {
    private Long competitionId;
    private String competitionName;
    private String clubName;
    private Long clubId;
    private Long appearance;
    private Long goals;
    private Long yellowCards;
    private Long redCards;
}
