package com.zestio.app.competition.services;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Component;

import com.zestio.app.competition.Competition;
import com.zestio.app.competition.dto.CompetitionResult;
import com.zestio.app.competition.dto.Result;
import com.zestio.app.match.MatchRepository;
import com.zestio.app.match.enums.MatchStatus;
import com.zestio.app.utils.Resolver;

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