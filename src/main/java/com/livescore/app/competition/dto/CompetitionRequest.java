package com.livescore.app.competition.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;

import com.livescore.app.competition.enums.CompetitionLegFormat;
import com.livescore.app.competition.enums.CompetitionScope;
import com.livescore.app.competition.enums.CompetitionStatus;
import com.livescore.app.competition.enums.CompetitionType;

@Getter
@Setter
public class CompetitionRequest {

    private String name;

    private String competitionCode;

    private String logoUrl;

    private CompetitionScope scope;

    private CompetitionStatus status;

    private CompetitionLegFormat legFormat;

    private Integer totalTeams;

    private CompetitionType competitionType;

    private Integer totalRounds;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Set<Long> teamIds;

    private Long leagueId;
}