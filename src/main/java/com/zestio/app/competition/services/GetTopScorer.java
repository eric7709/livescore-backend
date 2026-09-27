package com.zestio.app.competition.services;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import com.zestio.app.competition.dto.PlayerStatDTO;
import com.zestio.app.competition.helpers.BuildPlayerStats;
import com.zestio.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetTopScorer {
    private final Resolver resolver;
    private final BuildPlayerStats buildPlayerStats;

    @SuppressWarnings("null")
    public List<PlayerStatDTO> get(Long competitionId) {
        resolver.resolveCompetition(competitionId);
        return buildPlayerStats.buildPlayerStats(competitionId)
                .values()
                .stream()
                .filter(stat -> stat.getNumberOfGoals() > 0)
                .sorted(Comparator.comparingInt(PlayerStatDTO::getNumberOfGoals).reversed()
                        .thenComparing(Comparator.comparingInt(PlayerStatDTO::getNumberOfAssists).reversed())
                        .thenComparing(PlayerStatDTO::getName, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();
    }

}
