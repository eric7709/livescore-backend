package com.livescore.app.competition.dto;

import java.time.LocalDate;

import com.livescore.app.competition.enums.CompetitionLegFormat;
import com.livescore.app.competition.enums.CompetitionScope;
import com.livescore.app.competition.enums.CompetitionStatus;
import com.livescore.app.competition.enums.CompetitionType;

import lombok.Data;

@Data
public class CompetitionQueryParams {

    private Long leagueId;

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