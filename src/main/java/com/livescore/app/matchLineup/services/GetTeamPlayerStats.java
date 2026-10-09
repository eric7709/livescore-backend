package com.livescore.app.matchLineup.services;

import com.livescore.app.matchEvents.MatchEvent;
import com.livescore.app.matchEvents.MatchEventRepository;
import com.livescore.app.matchLineup.MatchLineup;
import com.livescore.app.matchLineup.dtos.PlayerLineupInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.livescore.app.matchLineup.utils.LineupSupport;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetTeamPlayerStats {

    private final MatchEventRepository matchEventRepository;
    private final LineupSupport lineupSupport;

    public List<PlayerLineupInfo> get(Long matchId, Long teamId) {
        MatchLineup lineup = lineupSupport.resolveTeamLineup(matchId, teamId);
        List<MatchEvent> events = matchEventRepository.findByMatchId(matchId);
        return lineupSupport.buildPlayerStats(lineup, events);
    }
}