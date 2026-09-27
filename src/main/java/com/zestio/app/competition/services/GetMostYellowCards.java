package com.zestio.app.competition.services;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.zestio.app.matchEvents.MatchEventRepository;
import com.zestio.app.matchEvents.enums.EventType;
import com.zestio.app.utils.Resolver;
import com.zestio.app.competition.dto.PlayerRankingDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMostYellowCards {
    private final Resolver resolver;
    private final MatchEventRepository matchEventRepository;

    public List<PlayerRankingDTO> get(Long competitionId, int limit) {
        resolver.resolveCompetition(competitionId);
        return matchEventRepository.findPlayerRankingByEventType(
                competitionId, EventType.YELLOW_CARD, PageRequest.of(0, limit));
    }
}