package com.livescore.app.competition.services;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.livescore.app.competition.dto.PlayerRankingDTO;
import com.livescore.app.matchEvents.MatchEventRepository;
import com.livescore.app.matchEvents.enums.EventType;
import com.livescore.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMostSaves {
    private final Resolver resolver;
    private final MatchEventRepository matchEventRepository;

    public List<PlayerRankingDTO> get(Long competitionId, int limit) {
        resolver.resolveCompetition(competitionId);
        return matchEventRepository.findPlayerRankingByEventType(
                competitionId, EventType.SAVE, PageRequest.of(0, limit));
    }
}
