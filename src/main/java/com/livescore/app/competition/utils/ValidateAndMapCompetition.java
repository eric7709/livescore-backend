package com.livescore.app.competition.utils;

import org.springframework.stereotype.Component;

import com.livescore.app.competition.Competition;
import com.livescore.app.competition.dto.CompetitionRequest;
import com.livescore.app.exceptions.BadRequestException;
import com.livescore.app.league.League;
import com.livescore.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ValidateAndMapCompetition {
    private final Resolver resolver;

    public void mapRequestToEntity(
            CompetitionRequest request,
            Competition competition) {
        competition.setName(request.getName());
        competition.setCompetitionCode(request.getCompetitionCode());
        competition.setLogoUrl(request.getLogoUrl());
        competition.setScope(request.getScope());
        competition.setTotalTeams(request.getTotalTeams());
        competition.setTotalRounds(request.getTotalRounds());
        competition.setStartDate(request.getStartDate());
        competition.setEndDate(request.getEndDate());
        competition.setCompetitionType(request.getCompetitionType());

        if (request.getStatus() != null) {
            competition.setStatus(request.getStatus());
        }

        if (request.getLegFormat() != null) {
            competition.setLegFormat(request.getLegFormat());
        }

        if (request.getTeamIds() != null) {
            competition.setTeams(
                    resolver.resolveTeams(request.getTeamIds()));
        }

        League league = resolver.resolveLeague(request.getLeagueId());
        competition.setLeague(league);
    }

    public void validateRequest(CompetitionRequest request) {

        if (request.getName() == null || request.getName().isBlank()) {
            throw new BadRequestException("name is required");
        }

        if (request.getCompetitionType() == null) {
            throw new BadRequestException("competitionType is required");
        }

        if (request.getLeagueId() == null) {
            throw new BadRequestException("leagueId is required");
        }
    }

}
