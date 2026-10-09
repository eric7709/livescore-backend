package com.livescore.app.matchEvents.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.matchEvents.MatchEventRepository;
import com.livescore.app.matchEvents.dtos.MatchEventDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class GetMatchEvent {
    private final MatchEventRepository matchEventRepository;

    @Transactional(readOnly = true)
    public List<MatchEventDTO> get(Long matchId) {
        return matchEventRepository.findByMatchId(matchId)
                .stream()
                .map(MatchEventDTO::fromEntity)
                .toList();
    }

}
