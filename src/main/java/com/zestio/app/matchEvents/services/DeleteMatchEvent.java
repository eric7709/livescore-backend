package com.zestio.app.matchEvents.services;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.zestio.app.exceptions.BadRequestException;
import com.zestio.app.match.Match;
import com.zestio.app.matchEvents.MatchEvent;
import com.zestio.app.matchEvents.MatchEventRepository;
import com.zestio.app.matchEvents.enums.EventType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteMatchEvent {
    private final MatchEventRepository matchEventRepository;

    public void delete(Long id) {
        MatchEvent event = matchEventRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Event couldnt be found"));
        Match match = event.getMatch();

        matchEventRepository.delete(event);
        recalculateScore(match);
    }

    private void recalculateScore(Match match) {
        int homeScore = 0;
        int awayScore = 0;
        for (MatchEvent event : match.getEvents()) {
            if (!GOAL_EVENT_TYPES.contains(event.getEventType())) {
                continue;
            }
            boolean eventTeamIsHome = event.getTeam().getId().equals(match.getHomeTeam().getId());
            boolean ownGoal = event.getEventType() == EventType.OWN_GOAL;

            if (eventTeamIsHome ^ ownGoal) {
                homeScore++;
            } else {
                awayScore++;
            }
        }

        match.setHomeScore(homeScore);
        match.setAwayScore(awayScore);
    }

    private static final Set<EventType> GOAL_EVENT_TYPES = Set.of(
            EventType.GOAL,
            EventType.OWN_GOAL,
            EventType.PENALTY_GOAL,
            EventType.FREE_KICK_GOAL,
            EventType.LONG_RANGE_GOAL);
}
