package com.livescore.app.matchEvents.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;
import com.livescore.app.match.Match;
import com.livescore.app.match.enums.MatchPeriod;
import com.livescore.app.matchEvents.MatchEvent;
import com.livescore.app.matchEvents.dtos.EventTypeCount;
import com.livescore.app.matchEvents.dtos.MatchStatistic;
import com.livescore.app.matchEvents.enums.EventType;
import com.livescore.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetMatchStatistics {

    private final Resolver resolver;

    private static final Set<EventType> STATISTICS_TYPE_EVENTS = Set.of(
            EventType.GOAL,
            EventType.YELLOW_CARD,
            EventType.RED_CARD,
            EventType.PENALTY_MISSED,
            EventType.CORNER,
            EventType.FREE_KICK,
            EventType.THROW_IN,
            EventType.GOAL_KICK,
            EventType.FOUL,
            EventType.OFFSIDE,
            EventType.SHOT_OFF_TARGET,
            EventType.SHOT_ON_TARGET,
            EventType.SAVE,
            EventType.SUBSTITUTION);

    private static final Set<EventType> GOAL_EVENT_TYPES = Set.of(
            EventType.GOAL,
            EventType.OWN_GOAL,
            EventType.PENALTY_GOAL,
            EventType.FREE_KICK_GOAL,
            EventType.LONG_RANGE_GOAL);

    private static final Set<EventType> GOAL_EVENT_TYPES_EX_OWN_GOAL = Set.of(
            EventType.GOAL,
            EventType.PENALTY_GOAL,
            EventType.FREE_KICK_GOAL,
            EventType.LONG_RANGE_GOAL);

    public List<MatchStatistic> getMatchStatistics(Long matchId) {
        Match match = resolver.resolveMatch(matchId);
        List<MatchStatistic> matchStatistics = getMatchStatisticsByPeriod(match);
        matchStatistics.add(buildStatistic(match, null));
        return matchStatistics;
    }

    private List<MatchStatistic> getMatchStatisticsByPeriod(Match match) {
        List<MatchStatistic> matchStatistics = new ArrayList<>();
        for (MatchPeriod period : match.getMatchPeriods()) {
            matchStatistics.add(buildStatistic(match, period));
        }
        return matchStatistics;
    }

    private MatchStatistic buildStatistic(Match match, MatchPeriod period) {
        Long homeTeamId = match.getHomeTeam().getId();
        Long awayTeamId = match.getAwayTeam().getId();
        List<MatchEvent> matchEvents = match.getEvents();
        MatchStatistic matchStatistic = new MatchStatistic();
        setPeriod(matchStatistic, period);
        boolean isPeriodNull = period == null;
        Set<EventTypeCount> eventTypeCounts = new HashSet<>();
        for (EventType eventType : EventType.values()) {
            long homeCount;
            long awayCount;
            EventType eventTypeInitial = null;
            if (GOAL_EVENT_TYPES.contains(eventType)) {
                homeCount = isPeriodNull ? countGoals(matchEvents, homeTeamId, awayTeamId)
                        : countGoals(matchEvents, homeTeamId, awayTeamId, period);
                awayCount = isPeriodNull ? countGoals(matchEvents, awayTeamId, homeTeamId)
                        : countGoals(matchEvents, awayTeamId, homeTeamId, period);
                eventTypeInitial = EventType.GOAL;
            } else if (eventType == EventType.RED_CARD || eventType == EventType.YELLOW_RED_CARD) {
                homeCount = isPeriodNull ? countByTeamAndTypes(matchEvents, homeTeamId,
                        Set.of(EventType.RED_CARD, EventType.YELLOW_RED_CARD))
                        : countByTeamAndTypes(matchEvents, homeTeamId,
                                Set.of(EventType.RED_CARD, EventType.YELLOW_RED_CARD), period);
                awayCount = isPeriodNull ? countByTeamAndTypes(matchEvents, awayTeamId,
                        Set.of(EventType.RED_CARD, EventType.YELLOW_RED_CARD))
                        : countByTeamAndTypes(matchEvents, awayTeamId,
                                Set.of(EventType.RED_CARD, EventType.YELLOW_RED_CARD), period);
                eventTypeInitial = EventType.RED_CARD;
            } else {
                homeCount = isPeriodNull ? countByTeamAndTypes(matchEvents, homeTeamId, Set.of(eventType))
                        : countByTeamAndTypes(matchEvents, homeTeamId, Set.of(eventType), period);
                awayCount = isPeriodNull ? countByTeamAndTypes(matchEvents, awayTeamId, Set.of(eventType))
                        : countByTeamAndTypes(matchEvents, awayTeamId, Set.of(eventType), period);
                eventTypeInitial = eventType;
            }
            EventTypeCount eventTypeCount = new EventTypeCount(eventTypeInitial, homeCount, awayCount);
            if (STATISTICS_TYPE_EVENTS.contains(eventTypeInitial)) {
                eventTypeCounts.add(eventTypeCount);
            }
        }
        matchStatistic.setStatistics(new ArrayList<>(eventTypeCounts));
        return matchStatistic;
    }

    private long countGoals(List<MatchEvent> events, Long scoringTeamId, Long opposingTeamId,
            MatchPeriod matchPeriod) {
        return events.stream()
                .filter(el -> el.getPeriod().equals(matchPeriod))
                .filter(el -> (el.getTeam().getId().equals(scoringTeamId)
                        && GOAL_EVENT_TYPES_EX_OWN_GOAL.contains(el.getEventType()))
                        || (el.getTeam().getId().equals(opposingTeamId)
                                && el.getEventType() == EventType.OWN_GOAL))
                .count();
    }

    private long countGoals(List<MatchEvent> events, Long scoringTeamId, Long opposingTeamId) {
        return events.stream()
                .filter(el -> (el.getTeam().getId().equals(scoringTeamId)
                        && GOAL_EVENT_TYPES_EX_OWN_GOAL.contains(el.getEventType()))
                        || (el.getTeam().getId().equals(opposingTeamId)
                                && el.getEventType() == EventType.OWN_GOAL))
                .count();
    }

    private long countByTeamAndTypes(List<MatchEvent> events, Long teamId, Set<EventType> eventTypes,
            MatchPeriod matchPeriod) {
        return events.stream()
                .filter(el -> el.getPeriod().equals(matchPeriod))
                .filter(el -> el.getTeam().getId().equals(teamId) && eventTypes.contains(el.getEventType()))
                .count();
    }

    private long countByTeamAndTypes(List<MatchEvent> events, Long teamId, Set<EventType> eventTypes) {
        return events.stream()
                .filter(el -> el.getTeam().getId().equals(teamId) && eventTypes.contains(el.getEventType()))
                .count();
    }

    private void setPeriod(MatchStatistic statistic, MatchPeriod period) {
        statistic.setPeriod(period);
        statistic.setPeriodLabel(periodLabel(period));
    }

    private String periodLabel(MatchPeriod period) {
        if (period == null) {
            return "Match";
        }
        return switch (period) {
            case FIRST_HALF -> "1ST HALF";
            case SECOND_HALF -> "2ND HALF";
            case EXTRA_TIME_FIRST_HALF -> "1ST HALF EXTRATIME";
            case EXTRA_TIME_SECOND_HALF -> "2ND HALF EXTRATIME";
            case PENALTIES -> "Penalties";
            default -> period.name();
        };
    }
}