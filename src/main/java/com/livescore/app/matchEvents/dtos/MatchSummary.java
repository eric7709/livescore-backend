package com.livescore.app.matchEvents.dtos;

import java.util.List;

import com.livescore.app.match.enums.MatchPeriod;

import lombok.Data;

@Data
public class MatchSummary {
    private MatchPeriod period;
    private String periodLabel;
    private Integer homeScore = 0;    
    private Integer awayScore = 0;
    private List<MatchEventDTO> summaries;
}