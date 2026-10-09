package com.livescore.app.competition.services;

import org.springframework.stereotype.Service;

import com.livescore.app.competition.Competition;
import com.livescore.app.competition.CompetitionRepository;
import com.livescore.app.competition.dto.CompetitionDTO;
import com.livescore.app.competition.dto.CompetitionRequest;
import com.livescore.app.competition.utils.ValidateAndMapCompetition;
import com.livescore.app.utils.Resolver;

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
