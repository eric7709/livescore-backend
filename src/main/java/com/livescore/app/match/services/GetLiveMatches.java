package com.livescore.app.match.services;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.livescore.app.match.Match;
import com.livescore.app.match.MatchRepository;
import com.livescore.app.match.enums.MatchStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetLiveMatches {

    private final MatchRepository matchRepository;

    /**
     * Returns the IDs of all teams currently playing in a live match
     * within the given competition.
     */
    public List<Long> get(Long competitionId) {
        List<Match> liveMatches = matchRepository.findByCompetitionIdAndStatus(
                competitionId, MatchStatus.LIVE
        );

        return liveMatches.stream()
                .flatMap(match -> Stream.of(match.getHomeTeam().getId(), match.getAwayTeam().getId()))
                .distinct()
                .collect(Collectors.toList());
    }
}