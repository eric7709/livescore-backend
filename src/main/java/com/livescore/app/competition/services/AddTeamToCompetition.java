package com.livescore.app.competition.services;

import org.springframework.stereotype.Service;

import com.livescore.app.competition.Competition;
import com.livescore.app.competition.CompetitionRepository;
import com.livescore.app.competition.dto.CompetitionDTO;
import com.livescore.app.team.Team;
import com.livescore.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddTeamToCompetition {
    private final Resolver resolver;
    private final CompetitionRepository competitionRepository;

    public CompetitionDTO add(Long competitionId, Long teamId) {
        Competition competition = resolver.resolveCompetition(competitionId);
        Team team = resolver.resolveTeam(teamId);
        competition.getTeams().add(team);
        return CompetitionDTO.fromEntity(competitionRepository.save(competition));
    }

}
