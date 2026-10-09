package com.livescore.app.competition.dto;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.livescore.app.match.Match;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Result {
    private String matchId;
    private String matchDate;
    private String matchTime;

    private String homeTeamId;
    private String homeTeamName;
    private String homeTeamCode;
    private String homeTeamLogoUrl;
    private Integer homeScore;

    private String awayTeamId;
    private String awayTeamName;
    private String awayTeamCode;
    private String awayTeamLogoUrl;
    private Integer awayScore;

    public static Result toResult(Match match) {
        return Result.builder()
                .matchId(match.getId().toString())
                .matchDate(DATE_FORMATTER.format(match.getMatchDate()))
                .matchTime(TIME_FORMATTER.format(match.getMatchDate()))

                .homeTeamId(match.getHomeTeam().getId().toString())
                .homeTeamName(match.getHomeTeam().getName())
                .homeTeamCode(match.getHomeTeam().getTeamCode())
                .homeTeamLogoUrl(match.getHomeTeam().getLogoUrl())
                .homeScore(match.getHomeScore())

                .awayTeamId(match.getAwayTeam().getId().toString())
                .awayTeamName(match.getAwayTeam().getName())
                .awayTeamCode(match.getAwayTeam().getTeamCode())
                .awayTeamLogoUrl(match.getAwayTeam().getLogoUrl())
                .awayScore(match.getAwayScore())

                .build();
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            .withZone(ZoneId.of("Africa/Lagos"));

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.of("Africa/Lagos"));

}