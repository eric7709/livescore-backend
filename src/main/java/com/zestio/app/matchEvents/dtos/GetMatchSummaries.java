package com.zestio.app.matchEvents.dtos;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zestio.app.match.Match;
import com.zestio.app.match.enums.MatchPeriod;
import com.zestio.app.matchEvents.MatchEvent;
import com.zestio.app.matchEvents.enums.EventType;
import com.zestio.app.utils.Resolver;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class GetMatchSummaries {
    private final Resolver resolver;

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public List<MatchSummary> getMatchSummaries(Long matchId) {
        Match match = resolver.resolveMatch(matchId);
        List<MatchPeriod> periods = match.getMatchPeriods();
        List<MatchEvent> summaryEvents = getSummaryEvents(match);
        List<MatchSummary> summaries = new ArrayList<>();
        for (MatchPeriod period : periods) {
            MatchSummary matchSummary = new MatchSummary();
            Integer homeScore = 0;
            Integer awayScore = 0;
            List<MatchEvent> periodEvents = new ArrayList<>();
            for (MatchEvent matchEvent : summaryEvents) {
                EventType eventType = matchEvent.getEventType();
                boolean isGoal = GOAL_EVENT_TYPES_EXCLUDING_OWN_GOAL.contains(eventType);
                boolean isOwnGoal = EventType.OWN_GOAL == eventType;
                boolean matchesPeriod = matchEvent.getPeriod() == period;
                if (matchesPeriod) {
                    boolean isHomeTeam = match.getHomeTeam().getId().equals(matchEvent.getTeam().getId());
                    if (isGoal) {
                        if (isHomeTeam) {
                            homeScore++;
                        } else {
                            awayScore++;
                        }
                    }
                    if (isOwnGoal) {
                        if (isHomeTeam) {
                            awayScore++;
                        } else {
                            homeScore++;
                        }
                    }
                    periodEvents.add(matchEvent);
                }
            }
            periodEvents.sort(
                    Comparator.comparing(MatchEvent::getMinute)
                            .thenComparing(MatchEvent::getSecond));
            List<MatchEventDTO> newEvents = periodEvents.stream()
                    .map(MatchEventDTO::fromEntity)
                    .toList();

            setPeriod(matchSummary, period);
            matchSummary.setSummaries(newEvents);
            matchSummary.setHomeScore(homeScore);
            matchSummary.setAwayScore(awayScore);
            summaries.add(matchSummary);
        }
        return summaries;
    }

    private List<MatchEvent> getSummaryEvents(Match match) {
        List<MatchEvent> matchEvents = match.getEvents();
        List<MatchEvent> summaryEvents = new ArrayList<>();
        for (MatchEvent matchEvent : matchEvents) {
            if (SUMMARY_EVENT_TYPES.contains(matchEvent.getEventType())) {
                summaryEvents.add(matchEvent);
            }
        }
        return summaryEvents;
    }

    private void setPeriod(MatchSummary summary, MatchPeriod period) {
        if (period == MatchPeriod.FIRST_HALF) {
            summary.setPeriod(period);
            summary.setPeriodLabel("1ST HALF");
        }
        if (period == MatchPeriod.SECOND_HALF) {
            summary.setPeriod(period);
            summary.setPeriodLabel("2ND HALF");
        }
        if (period == MatchPeriod.EXTRA_TIME_FIRST_HALF) {
            summary.setPeriod(period);
            summary.setPeriodLabel("1ST HALF EXTRATIME");
        }
        if (period == MatchPeriod.EXTRA_TIME_SECOND_HALF) {
            summary.setPeriod(period);
            summary.setPeriodLabel("2ND HALF EXTRATIME");
        }
        if (period == MatchPeriod.PENALTIES) {
            summary.setPeriod(period);
            summary.setPeriodLabel("Penalties");
        }
    }

    private static final Set<EventType> GOAL_EVENT_TYPES_EXCLUDING_OWN_GOAL = Set.of(
            EventType.GOAL,
            EventType.PENALTY_GOAL,
            EventType.FREE_KICK_GOAL,
            EventType.LONG_RANGE_GOAL);

    private static final Set<EventType> SUMMARY_EVENT_TYPES = Set.of(
            EventType.GOAL,
            EventType.OWN_GOAL,
            EventType.PENALTY_GOAL,
            EventType.FREE_KICK_GOAL,
            EventType.LONG_RANGE_GOAL,
            EventType.PENALTY_AWARDED,
            EventType.PENALTY_MISSED,
            EventType.YELLOW_CARD,
            EventType.YELLOW_RED_CARD,
            EventType.RED_CARD,
            EventType.SUBSTITUTION);
}