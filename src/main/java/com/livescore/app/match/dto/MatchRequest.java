package com.livescore.app.match.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

import com.livescore.app.match.enums.MatchGround;
import com.livescore.app.match.enums.MatchPeriod;
import com.livescore.app.match.enums.MatchStatus;
import com.livescore.app.match.enums.MatchTieBreaker;
import com.livescore.app.match.enums.MatchType;

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
    /** Required to finish a knockout match that is level (or, for leg 2, level on aggregate). */
    private MatchTieBreaker tieBreaker;
}
