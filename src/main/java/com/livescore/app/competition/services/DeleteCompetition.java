package com.livescore.app.competition.services;

import org.springframework.stereotype.Service;

import com.livescore.app.competition.CompetitionRepository;
import com.livescore.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteCompetition {
    private final CompetitionRepository competitionRepository;
    private final Resolver resolver;
    public void delete(Long id) {
        competitionRepository.delete(resolver.resolveCompetition(id));
    }

}
