package com.zestio.app.competition.services;

import org.springframework.stereotype.Service;

import com.zestio.app.competition.Competition;
import com.zestio.app.competition.CompetitionRepository;
import com.zestio.app.competition.dto.CompetitionDTO;
import com.zestio.app.competition.dto.CompetitionRequest;
import com.zestio.app.competition.helpers.ValidateAndMapCompetition;
import com.zestio.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateCompetition {
    private final CompetitionRepository competitionRepository;
    private final Resolver resolver;
    private final ValidateAndMapCompetition validateAndMapCompetition;

    public CompetitionDTO update(Long id, CompetitionRequest request) {
        validateAndMapCompetition.validateRequest(request);
        Competition competition = resolver.resolveCompetition(id);
        validateAndMapCompetition.mapRequestToEntity(request, competition);
        return CompetitionDTO.fromEntity(competitionRepository.save(competition));
    }

}
