package com.livescore.app.match.dto;

import java.time.Instant;

import com.livescore.app.match.enums.MatchResult;

public record TeamFormEntryDTO(
        Long matchId,
        Long homeTeamId,
        String homeTeamName,
        String homeTeamLogo,
        Long awayTeamId,
        String awayTeamName,
        String awayTeamLogo,
        Integer homeScore,
        Integer awayScore,
        String score,
        Long competitionId,
        String competitionName,
        String competitionLogoUrl,
        Instant matchDate,
        MatchResult result
) {}