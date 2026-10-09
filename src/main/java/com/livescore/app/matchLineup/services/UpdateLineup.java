package com.livescore.app.matchLineup.services;

import com.livescore.app.exceptions.BadRequestException;
import com.livescore.app.match.Match;
import com.livescore.app.match.utils.MatchBroadcastService;
import com.livescore.app.matchLineup.LineupPlayerRepository;
import com.livescore.app.matchLineup.MatchLineup;
import com.livescore.app.matchLineup.dtos.MatchLineUpRequest;
import com.livescore.app.matchLineup.dtos.MatchLineupDTO;
import com.livescore.app.team.Team;
import com.livescore.app.utils.Resolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.matchLineup.utils.LineupSupport;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateLineup {

    private final LineupPlayerRepository lineupPlayerRepository;
    private final Resolver resolver;
    private final LineupSupport lineupSupport;
    private final MatchBroadcastService matchBroadcastService;

    public MatchLineupDTO update(Long matchId, Long teamId, MatchLineUpRequest request) {
        lineupSupport.validateLineupRequestIds(request);
        if (!matchId.equals(request.getMatchId()) || !teamId.equals(request.getTeamId())) {
            throw new BadRequestException("Route match/team IDs must match the lineup request IDs");
        }
        Match match = resolver.resolveMatch(matchId);
        Team team = resolver.resolveTeam(teamId);
        MatchLineup lineup = lineupSupport.resolveTeamLineup(matchId, teamId);

        lineupSupport.verifyMatchIsValidForLineup(match);
        lineupSupport.verifyTeamParticipatesInMatch(match, team);
        lineupSupport.validateLineupPlayers(request, team);

        // Explicitly delete old rows so this remains correct whether or not the
        // JPA relation is configured with orphanRemoval = true.
        lineupPlayerRepository.deleteAll(new ArrayList<>(lineup.getPlayers()));
        lineup.getPlayers().clear();

        lineupSupport.applyLineupRequest(lineup, request);
        lineup.getPlayers().addAll(lineupSupport.saveLineupPlayers(request.getPlayers(), lineup));

        matchBroadcastService.broadcastLineup(lineup);

        return MatchLineupDTO.mapToDTO(lineup);
    }
}