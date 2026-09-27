package com.zestio.app.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CompetitionStatResponseDTO {
    private String id; // "{competitionId}-{clubId}"
    private Long competitionId;
    private String competitionName;
    private String competitionLogoUrl;
    private Long clubId;
    private String clubName;
    private String clubLogoUrl;
    private Integer appearances;
    private Integer goals;
    private Integer yellowCards;
    private Integer redCards;
}