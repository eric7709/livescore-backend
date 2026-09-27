package com.zestio.app.competition.services;

import org.springframework.stereotype.Service;

import com.zestio.app.competition.CompetitionRepository;
import com.zestio.app.utils.Resolver;

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
