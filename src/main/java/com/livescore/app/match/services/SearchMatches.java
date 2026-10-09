package com.livescore.app.match.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.match.Match;
import com.livescore.app.match.MatchRepository;
import com.livescore.app.match.dto.MatchDTO;
import com.livescore.app.match.dto.MatchSearchRequest;
import com.livescore.app.match.utils.MatchSpecification;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchMatches {
    private final MatchRepository matchRepository;

    @Transactional(readOnly = true)
    public Page<MatchDTO> search(MatchSearchRequest request, Pageable pageable) {
        Specification<Match> spec = MatchSpecification.search(request);
        return matchRepository.findAll(spec, pageable)
                .map(MatchDTO::fromEntity);
    }
}
