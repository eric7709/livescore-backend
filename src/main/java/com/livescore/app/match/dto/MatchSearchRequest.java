package com.livescore.app.match.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import com.livescore.app.match.enums.MatchPeriod;
import com.livescore.app.match.enums.MatchStatus;
import com.livescore.app.match.enums.MatchType;

@Getter
@Setter
public class MatchSearchRequest {
    private Long matchId;
    private Long leagueId;
    private Long competitionId;
    private Long teamId;
    private String stadium;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private LocalDate date;
    private MatchPeriod period;
    private MatchStatus status;
    private MatchType matchType;
}