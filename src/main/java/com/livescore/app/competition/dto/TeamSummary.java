package com.livescore.app.competition.dto;

import com.livescore.app.team.Team;

public record TeamSummary(
        Long id,
        String name,
        String logoUrl) {
    public static TeamSummary from(Team team) {
        if (team == null) {
            return null;
        }
        return new TeamSummary(team.getId(), team.getName(), team.getLogoUrl());
    }
}