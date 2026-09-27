package com.zestio.app.competition.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.zestio.app.competition.dto.PlayerStatDTO;
import com.zestio.app.matchEvents.MatchEvent;
import com.zestio.app.matchEvents.MatchEventRepository;
import com.zestio.app.matchEvents.enums.EventType;
import com.zestio.app.profile.Profile;
import com.zestio.app.team.Team;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BuildPlayerStats {
    private final MatchEventRepository matchEventRepository;

    public Map<Long, PlayerStatDTO> buildPlayerStats(Long competitionId) {
        List<MatchEvent> goalEvents = matchEventRepository
                .findByMatch_CompetitionIdAndEventTypeIn(
                        competitionId,
                        new ArrayList<>(PERSONAL_GOAL_EVENT_TYPES));

        Map<Long, PlayerStatDTO> statsByPlayer = new HashMap<>();

        for (MatchEvent event : goalEvents) {
            Profile scorer = event.getPrimaryPlayer();
            if (scorer != null) {
                PlayerStatDTO scorerStats = statsByPlayer.computeIfAbsent(
                        scorer.getId(),
                        ignored -> newPlayerStat(scorer, event.getTeam()));
                scorerStats.setNumberOfGoals(scorerStats.getNumberOfGoals() + 1);
            }

            if (!ASSIST_ELIGIBLE_GOAL_EVENT_TYPES.contains(event.getEventType())) {
                continue;
            }

            Profile assister = event.getSecondaryPlayer();
            if (assister != null) {
                PlayerStatDTO assisterStats = statsByPlayer.computeIfAbsent(
                        assister.getId(),
                        ignored -> newPlayerStat(assister, event.getTeam()));
                assisterStats.setNumberOfAssists(assisterStats.getNumberOfAssists() + 1);
            }
        }

        return statsByPlayer;
    }

    private PlayerStatDTO newPlayerStat(Profile player, Team team) {
        PlayerStatDTO stat = new PlayerStatDTO();
        stat.setPlayerId(player.getId());
        stat.setName(player.getFullName());
        stat.setTeamId(team.getId());
        stat.setTeamName(team.getName());
        stat.setTeamLogoUrl(team.getLogoUrl());
        return stat;
    }

    /** Personal goals only. OWN_GOAL is intentionally not included. */
    private static final Set<EventType> PERSONAL_GOAL_EVENT_TYPES = Set.of(
            EventType.GOAL,
            EventType.PENALTY_GOAL,
            EventType.FREE_KICK_GOAL,
            EventType.LONG_RANGE_GOAL);

    /** Only normal and long-range goals can receive an assist. */
    private static final Set<EventType> ASSIST_ELIGIBLE_GOAL_EVENT_TYPES = Set.of(
            EventType.GOAL,
            EventType.LONG_RANGE_GOAL);

}
