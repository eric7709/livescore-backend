package com.livescore.app.matchLineup.services;

import com.livescore.app.exceptions.BadRequestException;
import com.livescore.app.match.Match;
import com.livescore.app.matchEvents.MatchEvent;
import com.livescore.app.matchEvents.MatchEventRepository;
import com.livescore.app.matchLineup.MatchLineup;
import com.livescore.app.matchLineup.dtos.MatchPlayerStatsBothTeams;
import com.livescore.app.utils.Resolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.livescore.app.matchLineup.utils.LineupSupport;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMatchPlayerStats {

    private final Resolver resolver;
    private final MatchEventRepository matchEventRepository;
    private final LineupSupport lineupSupport;

    public MatchPlayerStatsBothTeams get(Long matchId) {
        Match match = resolver.resolveMatch(matchId);
        MatchLineup homeLineup = match.getHomeLineup();
        MatchLineup awayLineup = match.getAwayLineup();

        if (homeLineup == null || awayLineup == null) {
            throw new BadRequestException("Both teams must submit a lineup before player stats are available");
        }

        List<MatchEvent> events = matchEventRepository.findByMatchId(matchId);

        MatchPlayerStatsBothTeams response = new MatchPlayerStatsBothTeams();
        response.setMatchId(matchId);
        response.setHomeTeam(lineupSupport.buildPlayerStats(homeLineup, events));
        response.setAwayTeam(lineupSupport.buildPlayerStats(awayLineup, events));

        return response;
    }
}