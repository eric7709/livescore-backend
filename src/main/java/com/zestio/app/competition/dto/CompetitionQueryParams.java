package com.zestio.app.competition.dto;

import java.time.LocalDate;

import com.zestio.app.competition.enums.CompetitionLegFormat;
import com.zestio.app.competition.enums.CompetitionScope;
import com.zestio.app.competition.enums.CompetitionStatus;
import com.zestio.app.competition.enums.CompetitionType;

import lombok.Data;

@Data
public class CompetitionQueryParams {
    private Long teamId;
    private String competitionCode;
    private Long matchId;
    private String name;
    private CompetitionStatus status;
    private CompetitionScope scope;
    private CompetitionLegFormat legFormat;
    private CompetitionType competitionType;
    private LocalDate startDate;
    private LocalDate endDate;
}
