package com.livescore.app.competition.services;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Component;

import com.livescore.app.competition.Competition;
import com.livescore.app.competition.dto.CompetitionResult;
import com.livescore.app.competition.dto.Result;
import com.livescore.app.match.MatchRepository;
import com.livescore.app.match.enums.MatchStatus;
import com.livescore.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GetResultsForSpecificDate {

    private final Resolver resolver;
    private final MatchRepository matchRepository;
    private static final ZoneId ZONE = ZoneId.of("Africa/Lagos");

    public CompetitionResult get(LocalDate date, Long competitionId) {
        Competition competition = resolver.resolveCompetition(competitionId);

        List<Result> results = matchRepository
                .findByCompetitionIdAndStatusOrderByMatchDateDesc(competitionId, MatchStatus.FINISHED)
                .stream()
                .filter(m -> date == null || m.getMatchDate().atZone(ZONE).toLocalDate().equals(date))
                .map(el -> {
                    return Result.toResult(el);
                })
                .toList();

        return CompetitionResult.builder()
                .competitionId(competition.getId())
                .competitionName(competition.getName())
                .competitionCode(competition.getCompetitionCode())
                .competitionLogoUrl(competition.getLogoUrl())
                .results(results)
                .build();
    }

}