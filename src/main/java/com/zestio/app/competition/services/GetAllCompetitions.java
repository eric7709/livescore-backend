package com.zestio.app.competition.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zestio.app.competition.CompetitionRepository;
import com.zestio.app.competition.dto.CompetitionDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class GetAllCompetitions {
    private final CompetitionRepository competitionRepository;

    @Transactional(readOnly = true)
    public List<CompetitionDTO> get() {
        return competitionRepository.findAll()
                .stream()
                .map(CompetitionDTO::fromEntity)
                .toList();
    }

}
