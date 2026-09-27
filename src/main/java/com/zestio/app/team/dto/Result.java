package com.zestio.app.team.dto;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.zestio.app.match.Match;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Result {
    private String matchId;
    private String matchDate;
    private String matchTime;

    private String competitionId;
    private String competitionName;
    private String competitionCode;
    private String competitionLogoUrl;

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

    private String badge;

    public static Result toResult(Match match, Long teamId) {

        return Result.builder()
                .matchId(match.getId().toString())

                .matchDate(DATE_FORMATTER.format(match.getMatchDate()))
                .matchTime(TIME_FORMATTER.format(match.getMatchDate()))

                .competitionId(match.getCompetition().getId().toString())
                .competitionName(match.getCompetition().getName())
                .competitionCode(match.getCompetition().getCompetitionCode())
                .competitionLogoUrl(match.getCompetition().getLogoUrl())

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

                .badge(getBadge(match, teamId))

                .build();
    }

    private static String getBadge(Match match, Long teamId) {

        boolean isHome = match.getHomeTeam().getId().equals(teamId);

        int teamScore = isHome ? match.getHomeScore() : match.getAwayScore();
        int opponentScore = isHome ? match.getAwayScore() : match.getHomeScore();

        if (teamScore > opponentScore)
            return "W";
        if (teamScore < opponentScore)
            return "L";

        return "D";
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            .withZone(ZoneId.of("Africa/Lagos"));

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.of("Africa/Lagos"));

}
