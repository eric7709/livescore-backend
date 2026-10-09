package com.livescore.app.competition.services;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.livescore.app.competition.Competition;
import com.livescore.app.competition.CompetitionRepository;
import com.livescore.app.competition.dto.CompetitionDTO;
import com.livescore.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RemoveTeamsFromCompetition {
    private final Resolver resolver;
    private final CompetitionRepository competitionRepository;

    public CompetitionDTO remove(Long competitionId, Set<Long> teamIds) {
        Competition competition = resolver.resolveCompetition(competitionId);
        competition.getTeams().removeAll(resolver.resolveTeams(teamIds));
        return CompetitionDTO.fromEntity(competitionRepository.save(competition));
    }

}
