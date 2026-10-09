package com.livescore.app.matchEvents.dtos;

import com.livescore.app.match.enums.MatchPeriod;
import com.livescore.app.matchEvents.enums.EventType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MatchEventRequest {
    private Long matchId;
    private Long teamId;
    private Long primaryPlayerId;
    private Long secondaryPlayerId;
    private EventType eventType;
    private MatchPeriod period;
    private String eventData;
}