package com.zestio.app.competition.dto;

import com.zestio.app.competition.Competition;
import com.zestio.app.competition.enums.CompetitionStatus;
import com.zestio.app.competition.enums.CompetitionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionSummary {
    private Long id;
    private String name;
    private String competitionCode;
    private String logoUrl;
    private CompetitionType competitionType;
    private CompetitionStatus status;
    private Integer totalTeams;

    /**
     * Maps a Competition entity to a CompetitionSummary DTO.
     */
    public static CompetitionSummary fromEntity(Competition competition) {
        if (competition == null) {
            return null;
        }

        return CompetitionSummary.builder()
                .id(competition.getId())
                .name(competition.getName())
                .competitionCode(competition.getCompetitionCode())
                .logoUrl(competition.getLogoUrl())
                .competitionType(competition.getCompetitionType())
                .status(competition.getStatus())
                .totalTeams(competition.getTotalTeams())
                .build();
    }
}