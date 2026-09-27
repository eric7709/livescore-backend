package com.zestio.app.team.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.zestio.app.team.Team;
import com.zestio.app.match.Match;
import com.zestio.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Transactional
@Service
@RequiredArgsConstructor
public class GetTeamOpponent {
    private final Resolver resolver;

    @Transactional(readOnly = true)
    public Team get(Long id, Long matchId) {
        Match match = resolver.resolveMatch(matchId);
        Long opposingTeamId = match.getHomeTeam().getId().equals(id) ? match.getAwayTeam().getId()
                : match.getHomeTeam().getId();
        return resolver.resolveTeam(opposingTeamId);
    }

}
