package com.livescore.app.matchLineup.services;

import com.livescore.app.matchLineup.MatchLineup;
import com.livescore.app.matchLineup.dtos.MatchLineupDTO;
import com.livescore.app.matchLineup.utils.LineupSupport;
import com.livescore.app.utils.Resolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetTeamLineup {

    private final Resolver resolver;
    private final LineupSupport lineupSupport;

    public MatchLineupDTO get(Long matchId, Long teamId) {
        resolver.resolveTeam(teamId);
        MatchLineup lineup = lineupSupport.resolveTeamLineup(matchId, teamId);

        return MatchLineupDTO.mapToDTO(lineup);
    }
}