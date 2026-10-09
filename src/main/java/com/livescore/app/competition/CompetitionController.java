package com.livescore.app.competition;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.livescore.app.competition.dto.CompetitionDTO;
import com.livescore.app.competition.dto.CompetitionFixture;
import com.livescore.app.competition.dto.CompetitionQueryParams;
import com.livescore.app.competition.dto.CompetitionRequest;
import com.livescore.app.competition.dto.CompetitionResult;
import com.livescore.app.competition.dto.FixtureGenerationDTOs.FixtureGenerationResponse;
import com.livescore.app.competition.dto.FixtureGenerationDTOs.GenerateGroupStageRequest;
import com.livescore.app.competition.dto.FixtureGenerationDTOs.GenerateKnockoutFixturesRequest;
import com.livescore.app.competition.dto.FixtureGenerationDTOs.GenerateKnockoutRoundRequest;
import com.livescore.app.competition.dto.FixtureGenerationDTOs.GenerateLeagueFixturesRequest;
import com.livescore.app.competition.dto.FixtureGenerationDTOs.GenerateNextKnockoutRoundRequest;
import com.livescore.app.competition.dto.PlayerRankingDTO;
import com.livescore.app.competition.dto.PlayerStatDTO;
import com.livescore.app.competition.dto.TeamStandingDTO;
import com.livescore.app.competition.enums.CompetitionStatus;
import com.livescore.app.competition.services.AddTeamToCompetition;
import com.livescore.app.competition.services.AdvanceKnockoutStage;
import com.livescore.app.competition.services.AddTeamsToCompetition;
import com.livescore.app.competition.services.CreateCompetition;
import com.livescore.app.competition.services.DeleteCompetition;
import com.livescore.app.competition.services.GenerateCupFixtures;
import com.livescore.app.competition.services.GenerateKnockoutFixtures;
import com.livescore.app.competition.services.GenerateLeagueFixtures;
import com.livescore.app.competition.services.GetAllCompetitions;
import com.livescore.app.competition.services.GetCompetitionById;
import com.livescore.app.competition.services.GetCompetitionByStatus;
import com.livescore.app.competition.services.GetFixtureForSpecificDate;
import com.livescore.app.competition.services.GetLiveStandings;
import com.livescore.app.competition.services.GetMostRedCards;
import com.livescore.app.competition.services.GetMostSaves;
import com.livescore.app.competition.services.GetMostYellowCards;
import com.livescore.app.competition.services.GetResultsForSpecificDate;
import com.livescore.app.competition.services.GetStandings;
import com.livescore.app.competition.services.GetTopAssistProviders;
import com.livescore.app.competition.services.GetTopScorer;
import com.livescore.app.competition.services.RemoveTeamFromCompetition;
import com.livescore.app.competition.services.RemoveTeamsFromCompetition;
import com.livescore.app.competition.services.SearchCompetition;
import com.livescore.app.competition.services.UpdateCompetition;
import com.livescore.app.match.Match;

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
    private final GenerateLeagueFixtures generateLeagueFixtures;
    private final GenerateCupFixtures generateCupFixtures;
    private final GenerateKnockoutFixtures generateKnockoutFixtures;
    private final AdvanceKnockoutStage advanceKnockoutStage;

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

    // -------------------------------------------------------------------------
    // Fixture generation
    // -------------------------------------------------------------------------

    /** EPL style: single or double round-robin, decided by the competition's legFormat. */
    @PostMapping("/{competitionId}/fixtures/league")
    @ResponseStatus(HttpStatus.CREATED)
    public FixtureGenerationResponse generateLeagueFixtures(
            @PathVariable Long competitionId,
            @RequestBody GenerateLeagueFixturesRequest request) {
        List<Match> matches = generateLeagueFixtures.execute(
                competitionId,
                request.firstKickoff(),
                Duration.ofDays(request.roundIntervalDays()));
        return new FixtureGenerationResponse(getCompetitionById.get(competitionId), matches.size());
    }

    /**
     * Pure knockout: builds round 1 automatically from the competition's teams
     * (byes if not a power of two). Single or two-legged ties follow the
     * competition's legFormat. Later rounds generate themselves as matches finish.
     */
    @PostMapping("/{competitionId}/fixtures/knockout")
    @ResponseStatus(HttpStatus.CREATED)
    public FixtureGenerationResponse generateKnockoutFixtures(
            @PathVariable Long competitionId,
            @RequestBody GenerateKnockoutFixturesRequest request) {
        List<Match> matches = generateKnockoutFixtures.execute(
                competitionId,
                request.firstKickoff(),
                request.shuffleTeams());
        return new FixtureGenerationResponse(getCompetitionById.get(competitionId), matches.size());
    }

    /** World Cup style, step 1: splits teams into groups and schedules the group-stage round-robin. */
    @PostMapping("/{competitionId}/fixtures/cup/groups")
    @ResponseStatus(HttpStatus.CREATED)
    public FixtureGenerationResponse generateCupGroupStage(
            @PathVariable Long competitionId,
            @RequestBody GenerateGroupStageRequest request) {
        List<Match> matches = generateCupFixtures.generateGroupStage(
                competitionId,
                request.groupSize(),
                request.firstKickoff(),
                Duration.ofDays(request.roundIntervalDays()));
        return new FixtureGenerationResponse(getCompetitionById.get(competitionId), matches.size());
    }

    /**
     * World Cup style, step 2: call once group results are known, with the
     * qualifying team ids in bracket order (0v1, 2v3, ...) and round = 1.
     * For every round after this, prefer /cup/knockout/next (or let
     * AdvanceKnockoutStage do it automatically) instead of calling this again.
     */
    @PostMapping("/{competitionId}/fixtures/cup/knockout")
    @ResponseStatus(HttpStatus.CREATED)
    public FixtureGenerationResponse generateCupKnockoutRound(
            @PathVariable Long competitionId,
            @RequestBody GenerateKnockoutRoundRequest request) {
        List<Match> matches = generateCupFixtures.generateNextKnockoutRound(
                competitionId,
                request.orderedQualifierIds(),
                request.kickoff(),
                request.round());
        return new FixtureGenerationResponse(getCompetitionById.get(competitionId), matches.size());
    }

    /**
     * World Cup style, step 2+ (alternative to /cup/knockout): pass the
     * previous round's match ids instead of working out winners yourself —
     * this reads each match's winner off its score (falling back to
     * tieBreaker on a draw) and pairs them in the same order.
     */
    @PostMapping("/{competitionId}/fixtures/cup/knockout/next")
    @ResponseStatus(HttpStatus.CREATED)
    public FixtureGenerationResponse generateCupNextKnockoutRound(
            @PathVariable Long competitionId,
            @RequestBody GenerateNextKnockoutRoundRequest request) {
        List<Match> matches = generateCupFixtures.generateNextKnockoutRoundFromResults(
                competitionId,
                request.previousRoundMatchIds(),
                request.kickoff());
        return new FixtureGenerationResponse(getCompetitionById.get(competitionId), matches.size());
    }

    /**
     * Manual trigger for testing: checks whether the given match's knockout
     * round is now fully complete and, if so, generates the next round.
     * No-op (returns null) if the round isn't finished yet, the match isn't
     * a knockout match, or it was the final.
     *
     * Once your result-recording service exists, call
     * advanceKnockoutStage.advance(matchId) directly from there instead of
     * relying on this endpoint — see AdvanceKnockoutStage's class comment.
     */
    @PostMapping("/matches/{matchId}/advance-knockout")
    @ResponseStatus(HttpStatus.OK)
    public FixtureGenerationResponse advanceKnockout(@PathVariable Long matchId) {
        return advanceKnockoutStage.advance(matchId)
                .map(matches -> {
                    Long competitionId = matches.get(0).getCompetition().getId();
                    return new FixtureGenerationResponse(getCompetitionById.get(competitionId), matches.size());
                })
                .orElse(null);
    }
}