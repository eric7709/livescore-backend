package com.zestio.app.competition.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompetitionResult {
    private Long competitionId;
    private String competitionName;
    private String competitionCode;
    private String competitionLogoUrl;
    private List<Result> results;
}