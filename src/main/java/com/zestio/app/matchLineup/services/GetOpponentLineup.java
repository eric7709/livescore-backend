package com.zestio.app.matchLineup.services;

import com.zestio.app.exceptions.BadRequestException;
import com.zestio.app.match.Match;
import com.zestio.app.matchLineup.MatchLineupRepository;
import com.zestio.app.matchLineup.dtos.MatchLineupDTO;
import com.zestio.app.utils.Resolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetOpponentLineup {

    private final Resolver resolver;
    private final MatchLineupRepository matchLineupRepository;

    /** Returns the opponent's lineup, or null if they haven't submitted one yet. */
    public MatchLineupDTO get(Long matchId, Long teamId) {
        Match match = resolver.resolveMatch(matchId);
        Long homeId = match.getHomeTeam().getId();
        Long awayId = match.getAwayTeam().getId();

        Long opposingTeamId;
        if (homeId.equals(teamId)) {
            opposingTeamId = awayId;
        } else if (awayId.equals(teamId)) {
            opposingTeamId = homeId;
        } else {
            throw new BadRequestException("Team is not participating in this match");
        }

        return matchLineupRepository
                .findByMatchIdAndTeamId(matchId, opposingTeamId)
                .map(MatchLineupDTO::mapToDTO)
                .orElse(null);
    }
}