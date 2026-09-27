package com.zestio.app.competition.dto;

import com.zestio.app.competition.Competition;
import com.zestio.app.competition.enums.CompetitionScope;
import com.zestio.app.competition.enums.CompetitionStatus;

public record CompetitionSearchResult(
    Long id,
    String name,
    String competitionCode,
    String logoUrl,
    CompetitionScope scope,
    CompetitionStatus status
) {
    public static CompetitionSearchResult from(Competition competition) {
        if (competition == null) {
            return null;
        }
        return new CompetitionSearchResult(
            competition.getId(),
            competition.getName(),
            competition.getCompetitionCode(),
            competition.getLogoUrl(),
            competition.getScope(),
            competition.getStatus()
        );
    }
}