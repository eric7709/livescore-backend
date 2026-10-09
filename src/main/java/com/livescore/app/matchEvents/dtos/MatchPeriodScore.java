package com.livescore.app.matchEvents.dtos;

import com.livescore.app.match.enums.MatchPeriod;

import lombok.Data;

@Data
public class MatchPeriodScore {
    private Integer homeScore = 0;
    private Integer awayScore = 0;
    private MatchPeriod period; 
}
