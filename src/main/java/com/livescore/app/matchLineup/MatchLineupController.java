package com.livescore.app.matchLineup;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.livescore.app.matchLineup.dtos.MatchLineUpRequest;
import com.livescore.app.matchLineup.dtos.MatchLineupBothTeams;
import com.livescore.app.matchLineup.dtos.MatchLineupDTO;
import com.livescore.app.matchLineup.dtos.MatchPlayerStatsBothTeams;
import com.livescore.app.matchLineup.dtos.PlayerLineupInfo;
import com.livescore.app.matchLineup.services.GetAvailableSubstitutes;
import com.livescore.app.matchLineup.services.GetMatchLineup;
import com.livescore.app.matchLineup.services.GetMatchPlayerStats;
import com.livescore.app.matchLineup.services.GetTeamLineup;
import com.livescore.app.matchLineup.services.GetTeamPlayerStats;
import com.livescore.app.matchLineup.services.SubmitLineup;
import com.livescore.app.matchLineup.services.UpdateLineup;
import com.livescore.app.matchLineup.services.GetOpponentLineup;

import java.util.List;

@RestController
@RequestMapping("/api/match-lineup")
@RequiredArgsConstructor
public class MatchLineupController {

    private final SubmitLineup submitLineup;
    private final UpdateLineup updateLineup;
    private final GetMatchLineup getMatchLineup;
    private final GetTeamLineup getTeamLineup;
    private final GetTeamPlayerStats getTeamPlayerStats;
    private final GetMatchPlayerStats getMatchPlayerStats;
    private final GetOpponentLineup getOpponentLineup;
    private final GetAvailableSubstitutes getAvailableSubstitutes;

    // POST /api/match-lineup
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatchLineupDTO submitLineup(@RequestBody MatchLineUpRequest request) {
        return submitLineup.submit(request);
    }

    // GET /api/match-lineup/match/{matchId}
    @GetMapping("/match/{matchId}")
    @ResponseStatus(HttpStatus.OK)
    public MatchLineupBothTeams getMatchLineup(@PathVariable Long matchId) {
        return getMatchLineup.get(matchId);
    }

    // GET /api/match-lineup/match/{matchId}/team/{teamId}
    @GetMapping("/match/{matchId}/team/{teamId}")
    @ResponseStatus(HttpStatus.OK)
    public MatchLineupDTO getTeamLineup(@PathVariable Long matchId, @PathVariable Long teamId) {
        return getTeamLineup.get(matchId, teamId);
    }

    // GET /api/match-lineup/match/{matchId}/team/{teamId}
    @GetMapping("/match/{matchId}/team/{teamId}/opponent")
    @ResponseStatus(HttpStatus.OK)
    public MatchLineupDTO getOpponentLineup(@PathVariable Long matchId, @PathVariable Long teamId) {
        return getOpponentLineup.get(matchId, teamId);
    }

    // PUT /api/match-lineup/match/{matchId}/team/{teamId}
    @PutMapping("/match/{matchId}/team/{teamId}")
    @ResponseStatus(HttpStatus.OK)
    public MatchLineupDTO updateLineup(@PathVariable Long matchId,
            @PathVariable Long teamId,
            @RequestBody MatchLineUpRequest request) {
        return updateLineup.update(matchId, teamId, request);
    }

    // GET /api/match-lineup/match/{matchId}/team/{teamId}/stats
    @GetMapping("/match/{matchId}/team/{teamId}/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<PlayerLineupInfo> getTeamPlayerStats(@PathVariable Long matchId, @PathVariable Long teamId) {
        return getTeamPlayerStats.get(matchId, teamId);
    }

    // GET /api/match-lineup/match/{matchId}/stats
    @GetMapping("/match/{matchId}/stats")
    @ResponseStatus(HttpStatus.OK)
    public MatchPlayerStatsBothTeams getMatchPlayerStats(@PathVariable Long matchId) {
        return getMatchPlayerStats.get(matchId);
    }

    // GET /api/match-lineup/match/{matchId}/team/{teamId}/substitutes
    // Not previously exposed on this controller, but GetAvailableSubstitutes
    // had no caller anywhere else — wiring it in so it isn't dead code now
    // that MatchLineupService is gone. Remove this endpoint if it's actually
    // served from somewhere else.
    @GetMapping("/match/{matchId}/team/{teamId}/substitutes")
    @ResponseStatus(HttpStatus.OK)
    public List<PlayerLineupInfo> getAvailableSubstitutes(@PathVariable Long matchId, @PathVariable Long teamId) {
        return getAvailableSubstitutes.get(matchId, teamId);
    }
}