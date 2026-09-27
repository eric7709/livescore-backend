package com.zestio.app.match.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

import com.zestio.app.match.enums.MatchGround;
import com.zestio.app.match.enums.MatchPeriod;
import com.zestio.app.match.enums.MatchStatus;
import com.zestio.app.match.enums.MatchType;

@Getter
@Setter
public class MatchRequest {
    private Long id;
    private Long homeTeamId;
    private Long awayTeamId;
    private Long competitionId;
    private String stadium;
    private MatchGround ground;
    private Instant matchDate;
    private MatchType matchType;
    private MatchPeriod period;
    private MatchStatus status;
    private Instant startedAt;
    private Instant periodStartedAt;
}
