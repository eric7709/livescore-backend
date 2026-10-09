package com.livescore.app.match.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.match.MatchRepository;
import com.livescore.app.match.dto.MatchDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetAllMatches {
    private final MatchRepository matchRepository;

    @Transactional(readOnly = true)
    public List<MatchDTO> get() {
        return matchRepository.findAll()
                .stream()
                .map(MatchDTO::fromEntity)
                .collect(Collectors.toList());
    }

}
