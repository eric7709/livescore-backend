package com.livescore.app.competition.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

import com.livescore.app.competition.Competition;
import com.livescore.app.competition.enums.CompetitionLegFormat;
import com.livescore.app.competition.enums.CompetitionScope;
import com.livescore.app.competition.enums.CompetitionStatus;
import com.livescore.app.competition.enums.CompetitionType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionDTO {
    private Long id;
    private String name;
    private String competitionCode;
    private String logoUrl;
    private CompetitionScope scope; // Changed from String to enum
    private CompetitionStatus status;
    private CompetitionLegFormat legFormat;
    private Integer totalTeams;
    private CompetitionType competitionType;
    private Integer totalRounds;
    private Long leagueId;
    private String leagueName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer registeredTeamCount;
    private Set<Long> teamIds;

    public static CompetitionDTO fromEntity(Competition competition) {
        return CompetitionDTO.builder()
                .id(competition.getId())
                .name(competition.getName())
                .competitionCode(competition.getCompetitionCode())
                .logoUrl(competition.getLogoUrl())
                .scope(competition.getScope())
                .status(competition.getStatus())
                .legFormat(competition.getLegFormat())
                .totalTeams(competition.getTotalTeams())
                .competitionType(competition.getCompetitionType())
                .totalRounds(competition.getTotalRounds())
                .leagueId(competition.getLeague().getId())
                .leagueName(competition.getLeague().getName())
                .startDate(competition.getStartDate())
                .registeredTeamCount(competition.getTeams() != null ? competition.getTeams().size() : 0)
                .endDate(competition.getEndDate())
                .teamIds(competition.getTeams() != null
                        ? competition.getTeams().stream().map(t -> t.getId()).collect(Collectors.toSet())
                        : null)
                .build();
    }
}