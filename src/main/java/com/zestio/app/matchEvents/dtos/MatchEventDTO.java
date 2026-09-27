package com.zestio.app.matchEvents.dtos;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.Instant;

import com.zestio.app.match.Match;
import com.zestio.app.match.enums.MatchPeriod;
import com.zestio.app.matchEvents.MatchEvent;
import com.zestio.app.matchEvents.enums.EventType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchEventDTO {

    private Long id;
    private Long matchId;
    private Long teamId;
    private Long scoringTeamId;
    private Long primaryPlayerId;
    private String primaryPlayerName;
    private Long secondaryPlayerId;
    private String secondaryPlayerName;
    private EventType eventType;
    private MatchPeriod period;
    private Integer minute;
    private Integer second;
    private String eventData;
    private Instant createdAt;

    private static final java.util.Set<EventType> GOAL_EVENT_TYPES = java.util.Set.of(
            EventType.GOAL,
            EventType.OWN_GOAL,
            EventType.PENALTY_GOAL,
            EventType.FREE_KICK_GOAL,
            EventType.LONG_RANGE_GOAL);

    public static MatchEventDTO fromEntity(MatchEvent event) {
        return MatchEventDTO.builder()
                .id(event.getId())
                .matchId(event.getMatch() != null ? event.getMatch().getId() : null)
                .teamId(event.getTeam() != null ? event.getTeam().getId() : null)
                .scoringTeamId(resolveScoringTeamId(event))
                .primaryPlayerId(event.getPrimaryPlayer() != null ? event.getPrimaryPlayer().getId() : null)
                .primaryPlayerName(event.getPrimaryPlayer() != null ? event.getPrimaryPlayer().getFullName() : null)
                .secondaryPlayerId(event.getSecondaryPlayer() != null ? event.getSecondaryPlayer().getId() : null)
                .secondaryPlayerName(event.getSecondaryPlayer() != null ? event.getSecondaryPlayer().getFullName() : null)
                .eventType(event.getEventType())
                .period(event.getPeriod())
                .minute(event.getMinute())
                .second(event.getSecond())
                .eventData(event.getEventData())
                .createdAt(event.getCreatedAt())
                .build();
    }

    /**
     * Mirrors the home/away flip in MatchEventService#recordEvent /
     * #recalculateScore: for a normal goal the scoring team is the event's
     * own team; for an OWN_GOAL it's the opponent. Non-goal events have no
     * scoring team.
     */
    private static Long resolveScoringTeamId(MatchEvent event) {
        if (!GOAL_EVENT_TYPES.contains(event.getEventType())) {
            return null;
        }

        Match match = event.getMatch();
        if (match == null || event.getTeam() == null
                || match.getHomeTeam() == null || match.getAwayTeam() == null) {
            return event.getTeam() != null ? event.getTeam().getId() : null;
        }

        boolean eventTeamIsHome = event.getTeam().getId().equals(match.getHomeTeam().getId());
        boolean ownGoal = event.getEventType() == EventType.OWN_GOAL;
        boolean creditsHome = eventTeamIsHome ^ ownGoal;
        return creditsHome ? match.getHomeTeam().getId() : match.getAwayTeam().getId();
    }
}