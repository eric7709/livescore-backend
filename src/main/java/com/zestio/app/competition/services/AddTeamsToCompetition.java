package com.zestio.app.competition.services;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.zestio.app.competition.Competition;
import com.zestio.app.competition.CompetitionRepository;
import com.zestio.app.competition.dto.CompetitionDTO;
import com.zestio.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddTeamsToCompetition {
    private final Resolver resolver;
    private final CompetitionRepository competitionRepository;

    public CompetitionDTO add(Long competitionId, Set<Long> teamIds) {
        Competition competition = resolver.resolveCompetition(competitionId);
        competition.getTeams().addAll(resolver.resolveTeams(teamIds));
        return CompetitionDTO.fromEntity(competitionRepository.save(competition));
    }

}
