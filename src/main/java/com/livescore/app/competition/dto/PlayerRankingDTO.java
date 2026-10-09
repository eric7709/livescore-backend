package com.livescore.app.competition.dto;

public record PlayerRankingDTO(
        Long playerId,
        String name,
        Long teamId,
        String teamName,
        String teamLogoUrl,
        Long value
) {}