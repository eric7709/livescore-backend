package com.zestio.app.competition.dto;

import com.zestio.app.competition.Competition;
import com.zestio.app.competition.enums.CompetitionLegFormat;
import com.zestio.app.competition.enums.CompetitionScope;
import com.zestio.app.competition.enums.CompetitionStatus;
import com.zestio.app.competition.enums.CompetitionType;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record CompetitionDetail(
    Long id,
    String name,
    String competitionCode,
    String logoUrl,
    CompetitionScope scope,
    CompetitionStatus status,
    CompetitionLegFormat legFormat,
    CompetitionType competitionType,
    Integer totalTeams,
    Integer totalRounds,
    LocalDateTime startDate,
    LocalDateTime endDate,
    Set<TeamSummary> teams
) {
    public static CompetitionDetail from(Competition competition) {
        if (competition == null) {
            return null;
        }
        return new CompetitionDetail(
            competition.getId(),
            competition.getName(),
            competition.getCompetitionCode(),
            competition.getLogoUrl(),
            competition.getScope(),
            competition.getStatus(),
            competition.getLegFormat(),
            competition.getCompetitionType(),
            competition.getTotalTeams(),
            competition.getTotalRounds(),
            competition.getStartDate(),
            competition.getEndDate(),
            competition.getTeams().stream()
                .map(TeamSummary::from)
                .collect(Collectors.toUnmodifiableSet())
        );
    }
}