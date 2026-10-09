package com.livescore.app.competition.services;

import org.springframework.stereotype.Service;

import com.livescore.app.competition.Competition;
import com.livescore.app.competition.CompetitionRepository;
import com.livescore.app.competition.dto.CompetitionDTO;
import com.livescore.app.competition.dto.CompetitionRequest;
import com.livescore.app.competition.utils.ValidateAndMapCompetition;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CreateCompetition {
    private final CompetitionRepository competitionRepository;
    private final ValidateAndMapCompetition validateAndMapCompetition;

    public CompetitionDTO create(CompetitionRequest request) {
        validateAndMapCompetition.validateRequest(request);
        Competition competition = new Competition();
        validateAndMapCompetition.mapRequestToEntity(request, competition);
        return CompetitionDTO.fromEntity(competitionRepository.save(competition));
    }

}
