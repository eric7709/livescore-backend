package com.livescore.app.competition.services;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.livescore.app.competition.dto.PlayerStatDTO;
import com.livescore.app.competition.utils.BuildPlayerStats;
import com.livescore.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetTopAssistProviders {
    private final Resolver resolver;
    private final BuildPlayerStats buildPlayerStats;

    @SuppressWarnings("null")
    public List<PlayerStatDTO> get(Long competitionId) {
        resolver.resolveCompetition(competitionId);

        return buildPlayerStats.buildPlayerStats(competitionId)
                .values()
                .stream()
                .filter(stat -> stat.getNumberOfAssists() > 0)
                .sorted(Comparator.comparingInt(PlayerStatDTO::getNumberOfAssists).reversed()
                        .thenComparing(Comparator.comparingInt(PlayerStatDTO::getNumberOfGoals).reversed())
                        .thenComparing(PlayerStatDTO::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

}
