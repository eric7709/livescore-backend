package com.zestio.app.competition.services;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.zestio.app.competition.dto.PlayerRankingDTO;
import com.zestio.app.matchEvents.MatchEventRepository;
import com.zestio.app.matchEvents.enums.EventType;
import com.zestio.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMostRedCards {
    private final Resolver resolver;
    private final MatchEventRepository matchEventRepository;

    public List<PlayerRankingDTO> get(Long competitionId, int limit) {
        resolver.resolveCompetition(competitionId);
        return matchEventRepository.findPlayerRankingByEventTypes(
                competitionId,
                List.of(EventType.RED_CARD, EventType.YELLOW_RED_CARD),
                PageRequest.of(0, limit));
    }
}