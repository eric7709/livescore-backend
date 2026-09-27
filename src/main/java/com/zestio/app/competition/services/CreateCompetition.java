package com.zestio.app.competition.services;

import org.springframework.stereotype.Service;

import com.zestio.app.competition.Competition;
import com.zestio.app.competition.CompetitionRepository;
import com.zestio.app.competition.dto.CompetitionDTO;
import com.zestio.app.competition.dto.CompetitionRequest;
import com.zestio.app.competition.helpers.ValidateAndMapCompetition;

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
