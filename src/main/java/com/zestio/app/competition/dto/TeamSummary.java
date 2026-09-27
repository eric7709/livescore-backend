package com.zestio.app.competition.dto;

import com.zestio.app.team.Team;

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