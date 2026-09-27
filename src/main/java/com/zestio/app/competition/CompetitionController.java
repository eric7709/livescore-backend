package com.zestio.app.competition;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.zestio.app.competition.dto.CompetitionDTO;
import com.zestio.app.competition.dto.CompetitionFixture;
import com.zestio.app.competition.dto.CompetitionQueryParams;
import com.zestio.app.competition.dto.CompetitionRequest;
import com.zestio.app.competition.dto.CompetitionResult;
import com.zestio.app.competition.dto.PlayerRankingDTO;
import com.zestio.app.competition.dto.PlayerStatDTO;
import com.zestio.app.competition.dto.TeamStandingDTO;
import com.zestio.app.competition.enums.CompetitionStatus;
import com.zestio.app.competition.services.AddTeamToCompetition;
import com.zestio.app.competition.services.AddTeamsToCompetition;
import com.zestio.app.competition.services.CreateCompetition;
import com.zestio.app.competition.services.DeleteCompetition;
import com.zestio.app.competition.services.GetAllCompetitions;
import com.zestio.app.competition.services.GetCompetitionById;
import com.zestio.app.competition.services.GetCompetitionByStatus;
import com.zestio.app.competition.services.GetFixtureForSpecificDate;
import com.zestio.app.competition.services.GetLiveStandings;
import com.zestio.app.competition.services.GetMostRedCards;
import com.zestio.app.competition.services.GetMostSaves;
import com.zestio.app.competition.services.GetMostYellowCards;
import com.zestio.app.competition.services.GetResultsForSpecificDate;
import com.zestio.app.competition.services.GetStandings;
import com.zestio.app.competition.services.GetTopAssistProviders;
import com.zestio.app.competition.services.GetTopScorer;
import com.zestio.app.competition.services.RemoveTeamFromCompetition;
import com.zestio.app.competition.services.RemoveTeamsFromCompetition;
import com.zestio.app.competition.services.SearchCompetition;
import com.zestio.app.competition.services.UpdateCompetition;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/competitions")
@RequiredArgsConstructor
public class CompetitionController {

    private final CreateCompetition createCompetition;
    private final GetCompetitionById getCompetitionById;
    private final GetAllCompetitions getAllCompetitions;
    private final GetCompetitionByStatus getCompetitionByStatus;
    private final DeleteCompetition deleteCompetition;
    private final SearchCompetition searchCompetition;
    private final UpdateCompetition updateCompetition;
    private final RemoveTeamFromCompetition removeTeamFromCompetition;
    private final RemoveTeamsFromCompetition removeTeamsFromCompetition;
    private final AddTeamToCompetition addTeamToCompetition;
    private final AddTeamsToCompetition addTeamsToCompetition;
    private final GetTopAssistProviders getTopAssistProviders;
    private final GetTopScorer getTopScorer;
    private final GetLiveStandings getLiveStandings;
    private final GetStandings getStandings;
    private final GetFixtureForSpecificDate getFixtureForSpecificDate;
    private final GetResultsForSpecificDate getResultsForSpecificDate;
    private final GetMostSaves getMostSaves;
    private final GetMostYellowCards getMostYellowCards;
    private final GetMostRedCards getMostRedCards;

    // -------------------------------------------------------------------------
    // Competition CRUD
    // -------------------------------------------------------------------------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompetitionDTO create(@RequestBody CompetitionRequest request) {
        return createCompetition.create(request);
    }

    @GetMapping("/{id}")
    public CompetitionDTO getById(@PathVariable Long id) {
        return getCompetitionById.get(id);
    }

    @GetMapping
    public List<CompetitionDTO> getAll() {
        return getAllCompetitions.get();
    }

    @GetMapping("/search")
    public Page<CompetitionDTO> search(CompetitionQueryParams params, Pageable pageable) {
        return searchCompetition.search(params, pageable);
    }

    @GetMapping("/status/{status}")
    public List<CompetitionDTO> getByStatus(@PathVariable CompetitionStatus status) {
        return getCompetitionByStatus.get(status);
    }

    @PutMapping("/{id}")
    public CompetitionDTO update(@PathVariable Long id, @RequestBody CompetitionRequest request) {
        return updateCompetition.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deleteCompetition.delete(id);
    }

    // -------------------------------------------------------------------------
    // Competition teams
    // -------------------------------------------------------------------------

    @PostMapping("/{id}/teams/{teamId}")
    public CompetitionDTO addTeam(@PathVariable Long id, @PathVariable Long teamId) {
        return addTeamToCompetition.add(id, teamId);
    }

    @PostMapping("/{id}/teams")
    public CompetitionDTO addTeams(@PathVariable Long id, @RequestBody Set<Long> teamIds) {
        return addTeamsToCompetition.add(id, teamIds);
    }

    @DeleteMapping("/{id}/teams/{teamId}")
    public CompetitionDTO removeTeam(@PathVariable Long id, @PathVariable Long teamId) {
        return removeTeamFromCompetition.remove(id, teamId);
    }

    @DeleteMapping("/{id}/teams")
    public CompetitionDTO removeTeams(@PathVariable Long id, @RequestBody Set<Long> teamIds) {
        return removeTeamsFromCompetition.remove(id, teamIds);
    }

    // -------------------------------------------------------------------------
    // Tables
    // -------------------------------------------------------------------------

    @GetMapping("/{id}/standings")
    public List<TeamStandingDTO> getStandings(@PathVariable Long id) {
        return getStandings.get(id);
    }

    @GetMapping("/{id}/standings/live")
    public List<TeamStandingDTO> getLiveTable(@PathVariable Long id) {
        return getLiveStandings.get(id);
    }

    // -------------------------------------------------------------------------
    // Leaderboards
    // -------------------------------------------------------------------------

    @GetMapping("/{id}/top-scorers")
    public List<PlayerStatDTO> getTopScorers(@PathVariable Long id) {
        return getTopScorer.get(id);
    }

    @GetMapping("/{id}/top-assists")
    public List<PlayerStatDTO> getTopAssisters(@PathVariable Long id) {
        return getTopAssistProviders.get(id);
    }

    @GetMapping("/{competitionId}/most-saves")
    public ResponseEntity<List<PlayerRankingDTO>> mostSaves(
            @PathVariable Long competitionId,
            @RequestParam(defaultValue = "15") int limit) {
        return ResponseEntity.ok(getMostSaves.get(competitionId, limit));
    }

    @GetMapping("/{competitionId}/most-yellow-cards")
    public ResponseEntity<List<PlayerRankingDTO>> mostYellowCards(
            @PathVariable Long competitionId,
            @RequestParam(defaultValue = "15") int limit) {
        return ResponseEntity.ok(getMostYellowCards.get(competitionId, limit));
    }

    @GetMapping("/{competitionId}/most-red-cards")
    public ResponseEntity<List<PlayerRankingDTO>> mostRedCards(
            @PathVariable Long competitionId,
            @RequestParam(defaultValue = "15") int limit) {
        return ResponseEntity.ok(getMostRedCards.get(competitionId, limit));
    }

    // -------------------------------------------------------------------------
    // Results and Fixtures
    // -------------------------------------------------------------------------
    @GetMapping("/{competitionId}/fixtures")
    @ResponseStatus(HttpStatus.OK)
    public CompetitionFixture getFixtures(
            @PathVariable Long competitionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return getFixtureForSpecificDate.get(date, competitionId);
    }

    @GetMapping("/{competitionId}/results")
    @ResponseStatus(HttpStatus.OK)
    public CompetitionResult getResults(
            @PathVariable Long competitionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return getResultsForSpecificDate.get(date, competitionId);
    }
}
