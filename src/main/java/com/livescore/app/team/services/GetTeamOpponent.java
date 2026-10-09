package com.livescore.app.team.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.team.Team;
import com.livescore.app.match.Match;
import com.livescore.app.utils.Resolver;

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
