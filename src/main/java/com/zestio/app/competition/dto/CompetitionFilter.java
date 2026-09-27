package com.zestio.app.competition.dto;

import java.time.LocalDate;

import com.zestio.app.competition.enums.CompetitionScope;
import com.zestio.app.competition.enums.CompetitionStatus;
import com.zestio.app.competition.enums.CompetitionType;

public record CompetitionFilter(
    String query,
    CompetitionScope scope,
    CompetitionStatus status,
    CompetitionType type,
    LocalDate startDateFrom, // e.g., 2026-08-01
    LocalDate startDateTo,
    LocalDate endDateFrom,
    LocalDate endDateTo
) {}