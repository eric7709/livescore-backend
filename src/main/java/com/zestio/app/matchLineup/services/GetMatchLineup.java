package com.zestio.app.matchLineup.services;

import com.zestio.app.match.Match;
import com.zestio.app.matchLineup.dtos.MatchLineupBothTeams;
import com.zestio.app.matchLineup.dtos.MatchLineupDTO;
import com.zestio.app.utils.Resolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetMatchLineup {

    private final Resolver resolver;

    public MatchLineupBothTeams get(Long matchId) {
        Match match = resolver.resolveMatch(matchId);
        MatchLineupBothTeams response = new MatchLineupBothTeams();
        response.setMatchId(matchId);
        response.setHomeTeam(match.getHomeLineup() == null
                ? null
                : MatchLineupDTO.mapToDTO(match.getHomeLineup()));
        response.setAwayTeam(match.getAwayLineup() == null
                ? null
                : MatchLineupDTO.mapToDTO(match.getAwayLineup()));

        return response;
    }
}