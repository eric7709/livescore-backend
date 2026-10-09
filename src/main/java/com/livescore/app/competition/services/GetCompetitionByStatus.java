package com.livescore.app.competition.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.competition.CompetitionRepository;
import com.livescore.app.competition.dto.CompetitionDTO;
import com.livescore.app.competition.enums.CompetitionStatus;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor

public class GetCompetitionByStatus {
    private final CompetitionRepository competitionRepository;

    @Transactional(readOnly = true)
    public List<CompetitionDTO> get(CompetitionStatus status) {
        return competitionRepository.findByStatus(status)
                .stream()
                .map(CompetitionDTO::fromEntity)
                .toList();
    }

}
