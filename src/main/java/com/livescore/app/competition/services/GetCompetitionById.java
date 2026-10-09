package com.livescore.app.competition.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.competition.dto.CompetitionDTO;
import com.livescore.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class GetCompetitionById {
    private final Resolver resolver;

    @Transactional(readOnly = true)
    public CompetitionDTO get(Long id) {
        return CompetitionDTO.fromEntity(resolver.resolveCompetition(id));
    }

}
