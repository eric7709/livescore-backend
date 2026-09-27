package com.zestio.app.match.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

import com.zestio.app.match.enums.MatchPeriod;
import com.zestio.app.match.enums.MatchStatus;
import com.zestio.app.match.enums.MatchType;

@Getter
@Setter
public class MatchSearchRequest {
    private Long matchId;
    private Long competitionId;
    private Long teamId;
    private String stadium;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private MatchPeriod period;
    private MatchStatus status;
    private MatchType matchType;
}