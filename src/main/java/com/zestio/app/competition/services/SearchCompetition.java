package com.zestio.app.competition.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zestio.app.competition.Competition;
import com.zestio.app.competition.CompetitionRepository;
import com.zestio.app.competition.dto.CompetitionDTO;
import com.zestio.app.competition.dto.CompetitionQueryParams;
import com.zestio.app.competition.helpers.CompetitionSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SearchCompetition {
    private final CompetitionRepository competitionRepository;

    @Transactional(readOnly = true)
    public Page<CompetitionDTO> search(CompetitionQueryParams request, Pageable pageable) {
        Specification<Competition> specification = CompetitionSpecification.search(request);
        return competitionRepository.findAll(specification, pageable).map(CompetitionDTO::fromEntity);
    }

}
