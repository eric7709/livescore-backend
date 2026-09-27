package com.zestio.app.match.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.zestio.app.competition.Competition;
import com.zestio.app.competition.dto.CompetitionSummary;
import com.zestio.app.match.Match;
import com.zestio.app.match.MatchRepository;
import com.zestio.app.match.dto.CompetitionMatchesDTO;
import com.zestio.app.match.dto.MatchSummary;
import com.zestio.app.match.enums.MatchStatus;

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
    public List<CompetitionMatchesDTO> get(LocalDate date, List<MatchStatus> statuses) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now(ZoneOffset.UTC);
        Instant startOfDay = targetDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfDay = targetDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<Match> matches = (statuses == null || statuses.isEmpty())
                ? matchRepository.findByMatchDateBetweenWithCompetition(startOfDay, endOfDay)
                : matchRepository.findByMatchDateBetweenAndStatusInWithCompetition(startOfDay, endOfDay, statuses);

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
                            .build()
            );

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
                competition.getTotalTeams()
        );
    }
}