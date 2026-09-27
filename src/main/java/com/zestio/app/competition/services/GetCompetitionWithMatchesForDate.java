package com.zestio.app.competition.services;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.zestio.app.competition.CompetitionRepository;
import com.zestio.app.competition.dto.CompetitionAndMatches;
import com.zestio.app.match.Match;
import com.zestio.app.match.MatchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetCompetitionWithMatchesForDate {
    private final CompetitionRepository competitionRepository;
    private final MatchRepository matchRepository;
    /**
     * Returns competitions that have at least one match on the given date,
     * each paired with the list of matches scheduled that day.
     */
    public List<CompetitionAndMatches> get(LocalDate date) {
        Instant startOfDay = date.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endOfDay = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<Match> matches = matchRepository.findByMatchDateBetweenOrderByMatchDateAsc(startOfDay, endOfDay);
        if (matches.isEmpty()) {
            return List.of();
        }
        Map<Long, List<Match>> matchesByCompetitionId = matches.stream()
                .collect(Collectors.groupingBy(match -> match.getCompetition().getId()));
        List<CompetitionAndMatches> result = new ArrayList<>();
        for (Map.Entry<Long, List<Match>> entry : matchesByCompetitionId.entrySet()) {
            competitionRepository.findById(entry.getKey())
                    .ifPresent(competition ->
                            result.add(CompetitionAndMatches.toDTO(competition, entry.getValue())));
        }
        return result;
    }
}