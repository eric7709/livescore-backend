package com.livescore.app.team.dto;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.livescore.app.match.Match;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Fixture {

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

    private String awayTeamId;
    private String awayTeamName;
    private String awayTeamCode;
    private String awayTeamLogoUrl;

    private static final ZoneId ZONE = ZoneId.of("Africa/Lagos");

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy")
                    .withZone(ZONE);

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm")
                    .withZone(ZONE);

    public static Fixture toFixture(Match match) {

        return Fixture.builder()

                .matchId(
                        match.getId() != null
                                ? match.getId().toString()
                                : null
                )

                .matchDate(
                        match.getMatchDate() != null
                                ? DATE_FORMATTER.format(match.getMatchDate())
                                : null
                )

                .matchTime(
                        match.getMatchDate() != null
                                ? TIME_FORMATTER.format(match.getMatchDate())
                                : null
                )

                .competitionId(
                        match.getCompetition() != null &&
                        match.getCompetition().getId() != null
                                ? match.getCompetition().getId().toString()
                                : null
                )

                .competitionName(
                        match.getCompetition() != null
                                ? match.getCompetition().getName()
                                : null
                )

                .competitionCode(
                        match.getCompetition() != null
                                ? match.getCompetition().getCompetitionCode()
                                : null
                )

                .competitionLogoUrl(
                        match.getCompetition() != null
                                ? match.getCompetition().getLogoUrl()
                                : null
                )

                .homeTeamId(
                        match.getHomeTeam() != null &&
                        match.getHomeTeam().getId() != null
                                ? match.getHomeTeam().getId().toString()
                                : null
                )

                .homeTeamName(
                        match.getHomeTeam() != null
                                ? match.getHomeTeam().getName()
                                : null
                )

                .homeTeamCode(
                        match.getHomeTeam() != null
                                ? match.getHomeTeam().getTeamCode()
                                : null
                )

                .homeTeamLogoUrl(
                        match.getHomeTeam() != null
                                ? match.getHomeTeam().getLogoUrl()
                                : null
                )

                .awayTeamId(
                        match.getAwayTeam() != null &&
                        match.getAwayTeam().getId() != null
                                ? match.getAwayTeam().getId().toString()
                                : null
                )

                .awayTeamName(
                        match.getAwayTeam() != null
                                ? match.getAwayTeam().getName()
                                : null
                )

                .awayTeamCode(
                        match.getAwayTeam() != null
                                ? match.getAwayTeam().getTeamCode()
                                : null
                )

                .awayTeamLogoUrl(
                        match.getAwayTeam() != null
                                ? match.getAwayTeam().getLogoUrl()
                                : null
                )

                .build();
    }
}