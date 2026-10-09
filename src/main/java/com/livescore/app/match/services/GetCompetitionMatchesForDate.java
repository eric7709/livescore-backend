package com.livescore.app.match.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.livescore.app.competition.Competition;
import com.livescore.app.competition.dto.CompetitionSummary;
import com.livescore.app.match.Match;
import com.livescore.app.match.MatchRepository;
import com.livescore.app.match.dto.CompetitionMatchesDTO;
import com.livescore.app.match.dto.MatchSummary;
import com.livescore.app.match.enums.MatchStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCompetitionMatchesForDate {

    private final MatchRepository matchRepository;

    /**
     * Fetches matches on the given date (optionally filtered by status)
     * and groups them by competition into CompetitionMatchesDTO entries.
     * If date is null, defaults to today (UTC). If statuses is null or
     * empty, all statuses are included.
     */
    public List<CompetitionMatchesDTO> get(LocalDate date, List<MatchStatus> statuses, Long leagueId) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now(ZoneOffset.UTC);
        Instant startOfDay = targetDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfDay = targetDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        boolean hasStatuses = statuses != null && !statuses.isEmpty();
        boolean hasLeague = leagueId != null;

        List<Match> matches;
        if (!hasStatuses && !hasLeague) {
            matches = matchRepository.findByMatchDateBetweenWithCompetition(startOfDay, endOfDay);
        } else if (hasStatuses && !hasLeague) {
            matches = matchRepository.findByMatchDateBetweenAndStatusInWithCompetition(startOfDay, endOfDay, statuses);
        } else if (!hasStatuses) {
            matches = matchRepository.findByMatchDateBetweenAndLeagueWithCompetition(startOfDay, endOfDay, leagueId);
        } else {
            matches = matchRepository.findByMatchDateBetweenAndStatusInAndLeagueWithCompetition(startOfDay, endOfDay,
                    statuses, leagueId);
        }

        return groupMatchesByCompetition(matches);
    }

    private List<CompetitionMatchesDTO> groupMatchesByCompetition(List<Match> matches) {
        Map<Long, CompetitionMatchesDTO> grouped = new LinkedHashMap<>();

        for (Match match : matches) {
            Competition competition = match.getCompetition();
            if (competition == null) {
                continue;
            }

            CompetitionMatchesDTO dto = grouped.computeIfAbsent(
                    competition.getId(),
                    id -> CompetitionMatchesDTO.builder()
                            .competition(toCompetitionSummary(competition))
                            .matches(new ArrayList<>())
                            .build());

            dto.getMatches().add(MatchSummary.fromEntity(match));
        }

        return new ArrayList<>(grouped.values());
    }

    private CompetitionSummary toCompetitionSummary(Competition competition) {
        return new CompetitionSummary(
                competition.getId(),
                competition.getName(),
                competition.getCompetitionCode(),
                competition.getLogoUrl(),
                competition.getCompetitionType(),
                competition.getStatus(),
                competition.getTotalTeams());
    }
}