package com.zestio.app.matchLineup.services;

import com.zestio.app.exceptions.BadRequestException;
import com.zestio.app.match.Match;
import com.zestio.app.match.helpers.MatchBroadcastService;
import com.zestio.app.matchLineup.MatchLineup;
import com.zestio.app.matchLineup.MatchLineupRepository;
import com.zestio.app.matchLineup.dtos.MatchLineUpRequest;
import com.zestio.app.matchLineup.dtos.MatchLineupDTO;
import com.zestio.app.team.Team;
import com.zestio.app.utils.Resolver;
import com.zestio.app.matchLineup.helper.LineupSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SubmitLineup {

    private final MatchLineupRepository matchLineupRepository;
    private final Resolver resolver;
    private final LineupSupport lineupSupport;
    private final MatchBroadcastService matchBroadcastService;

    public MatchLineupDTO submit(MatchLineUpRequest request) {
        lineupSupport.validateLineupRequestIds(request);
        if (matchLineupRepository.findByMatchIdAndTeamId(request.getMatchId(), request.getTeamId()).isPresent()) {
            throw new BadRequestException("A lineup has already been submitted for this team in this match");
        }
        Match match = resolver.resolveMatch(request.getMatchId());
        Team team = resolver.resolveTeam(request.getTeamId());
        lineupSupport.verifyMatchIsValidForLineup(match);
        lineupSupport.verifyTeamParticipatesInMatch(match, team);
        lineupSupport.validateLineupPlayers(request, team);

        MatchLineup lineup = new MatchLineup();
        lineup.setMatch(match);
        lineup.setTeam(team);
        lineupSupport.applyLineupRequest(lineup, request);

        MatchLineup savedLineup = matchLineupRepository.save(lineup);
        savedLineup.getPlayers().addAll(lineupSupport.saveLineupPlayers(request.getPlayers(), savedLineup));

        matchBroadcastService.broadcastLineup(savedLineup);

        return MatchLineupDTO.mapToDTO(savedLineup);
    }
}