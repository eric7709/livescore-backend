package com.zestio.app.competition.dto;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.zestio.app.match.Match;
import com.zestio.app.match.enums.MatchStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Fixture {
    private String matchId;
    private String matchDate;
    private String matchTime;
    private String homeTeamId;
    private String homeTeamName;
    private String homeTeamCode;
    private String homeTeamLogoUrl;
    private String awayTeamId;
    private String awayTeamName;
    private MatchStatus status;
    private String awayTeamCode;
    private String awayTeamLogoUrl;

    public static Fixture toFixture(Match match) {
        return Fixture.builder()
                .matchId(match.getId().toString())
                .matchDate(DATE_FORMATTER.format(match.getMatchDate()))
                .matchTime(TIME_FORMATTER.format(match.getMatchDate()))
                .homeTeamId(match.getHomeTeam().getId().toString())
                .homeTeamName(match.getHomeTeam().getName())
                .homeTeamCode(match.getHomeTeam().getTeamCode())
                .homeTeamLogoUrl(match.getHomeTeam().getLogoUrl())
                .awayTeamId(match.getAwayTeam().getId().toString())
                .awayTeamName(match.getAwayTeam().getName())
                .awayTeamCode(match.getAwayTeam().getTeamCode())
                .status(match.getStatus())
                .awayTeamLogoUrl(match.getAwayTeam().getLogoUrl())
                .build();
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            .withZone(ZoneId.of("Africa/Lagos"));

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.of("Africa/Lagos"));

}