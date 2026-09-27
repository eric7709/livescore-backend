package com.zestio.app.profile.services;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zestio.app.competition.Competition;
import com.zestio.app.match.Match;
import com.zestio.app.matchEvents.MatchEvent;
import com.zestio.app.matchEvents.MatchEventRepository;
import com.zestio.app.matchEvents.enums.EventType;
import com.zestio.app.matchLineup.LineupPlayer;
import com.zestio.app.matchLineup.LineupPlayerRepository;
import com.zestio.app.matchLineup.enums.LineupStatus;
import com.zestio.app.profile.dto.CompetitionStatResponseDTO;
import com.zestio.app.team.Team;

import lombok.RequiredArgsConstructor;

/**
 * Builds a player's "Statistics by Competition" table: appearances, goals,
 * and card counts, grouped by competition + the club they represented.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetCompetitionStats {

    // Event types that count as a goal for the scorer. OWN_GOAL is
    // deliberately excluded — it's a goal for the opposing team, not a
    // stat for the player who put it in their own net.
    private static final Set<EventType> GOAL_EVENT_TYPES = Set.of(
            EventType.GOAL,
            EventType.PENALTY_GOAL,
            EventType.FREE_KICK_GOAL,
            EventType.LONG_RANGE_GOAL);

    private final LineupPlayerRepository lineupPlayerRepository;
    private final MatchEventRepository matchEventRepository;

    public List<CompetitionStatResponseDTO> get(Long playerId) {

        Map<String, Accumulator> stats = new LinkedHashMap<>();

        applyAppearances(playerId, stats);
        applyGoalsAndCards(playerId, stats);

        return stats.values().stream()
                .map(acc -> acc.toDto())
                .toList();
    }

    private void applyAppearances(Long playerId, Map<String, Accumulator> stats) {

        List<LineupPlayer> lineupEntries = lineupPlayerRepository.findByPlayerId(playerId).stream()
                .filter(entry -> entry.getStatus() != LineupStatus.MISSING)
                .toList();

        // Matches where this player actually came off the bench.
        // primaryPlayer on a SUBSTITUTION event is the player coming ON.
        Set<Long> subbedOnMatchIds = matchEventRepository
                .findByPrimaryPlayerIdAndEventType(playerId, EventType.SUBSTITUTION).stream()
                .map(event -> event.getMatch().getId())
                .collect(Collectors.toSet());

        for (LineupPlayer entry : lineupEntries) {

            Match match = entry.getMatchLineup().getMatch();

            boolean appeared = entry.getStatus() == LineupStatus.STARTER
                    || subbedOnMatchIds.contains(match.getId());

            if (!appeared) {
                continue;
            }

            Competition competition = match.getCompetition();
            Team club = entry.getMatchLineup().getTeam();

            if (competition == null || club == null) {
                continue; // orphaned/legacy data — nothing sensible to group it under
            }

            resolve(stats, competition, club).appearances++;
        }
    }

    private void applyGoalsAndCards(Long playerId, Map<String, Accumulator> stats) {

        List<MatchEvent> events = matchEventRepository.findByPrimaryPlayerId(playerId);

        for (MatchEvent event : events) {

            Match match = event.getMatch();
            Team team = event.getTeam();
            Competition competition = match.getCompetition();

            if (competition == null || team == null) {
                continue;
            }

            Accumulator acc = resolve(stats, competition, team);
            EventType type = event.getEventType();

            if (GOAL_EVENT_TYPES.contains(type)) {
                acc.goals++;
            } else if (type == EventType.YELLOW_CARD) {
                acc.yellowCards++;
            } else if (type == EventType.RED_CARD) {
                acc.redCards++;
            } else if (type == EventType.YELLOW_RED_CARD) {
                // Second yellow: tallied as both a yellow and the resulting
                // sending-off. Flip this if you'd rather it only count once.
                acc.yellowCards++;
                acc.redCards++;
            }
        }
    }

    private Accumulator resolve(Map<String, Accumulator> stats, Competition competition, Team club) {
        String key = competition.getId() + "-" + club.getId();
        return stats.computeIfAbsent(key, k -> new Accumulator(competition, club));
    }

    private static class Accumulator {
        private final Competition competition;
        private final Team club;
        private int appearances = 0;
        private int goals = 0;
        private int yellowCards = 0;
        private int redCards = 0;

        Accumulator(Competition competition, Team club) {
            this.competition = competition;
            this.club = club;
        }

        CompetitionStatResponseDTO toDto() {
            return CompetitionStatResponseDTO.builder()
                    .id(competition.getId() + "-" + club.getId())
                    .competitionId(competition.getId())
                    .competitionName(competition.getName())
                    .competitionLogoUrl(competition.getLogoUrl())
                    .clubId(club.getId())
                    .clubName(club.getName())
                    .clubLogoUrl(club.getLogoUrl())
                    .appearances(appearances)
                    .goals(goals)
                    .yellowCards(yellowCards)
                    .redCards(redCards)
                    .build();
        }
    }
}