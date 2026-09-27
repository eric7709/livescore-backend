package com.zestio.app.matchLineup.services;

import com.zestio.app.exceptions.BadRequestException;
import com.zestio.app.match.Match;
import com.zestio.app.matchEvents.MatchEvent;
import com.zestio.app.matchEvents.MatchEventRepository;
import com.zestio.app.matchLineup.MatchLineup;
import com.zestio.app.matchLineup.dtos.MatchPlayerStatsBothTeams;
import com.zestio.app.utils.Resolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.zestio.app.matchLineup.helper.LineupSupport;

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