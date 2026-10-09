package com.livescore.app.match.services;

import java.time.Instant;

import java.util.EnumSet;
import java.util.List;

import java.util.Set;

import org.springframework.stereotype.Service;

import com.livescore.app.competition.Competition;
import com.livescore.app.competition.utils.KnockoutTieResolver;
import com.livescore.app.competition.services.AdvanceKnockoutStage;
import com.livescore.app.competition.services.UpdateCompetitionStatus;
import com.livescore.app.exceptions.BadRequestException;
import com.livescore.app.match.Match;
import com.livescore.app.match.MatchRepository;
import com.livescore.app.match.dto.MatchDTO;
import com.livescore.app.match.dto.MatchRequest;
import com.livescore.app.match.enums.MatchPeriod;
import com.livescore.app.match.enums.MatchStatus;
import com.livescore.app.match.enums.MatchTieBreaker;
import com.livescore.app.match.enums.MatchType;
import com.livescore.app.match.utils.MatchBroadcastService;
import com.livescore.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateMatch {
    private final Resolver resolver;
    private final MatchRepository matchRepository;
    private final MatchBroadcastService matchBroadcastService;
    private final AdvanceKnockoutStage advanceKnockoutStage;
    private final UpdateCompetitionStatus updateCompetitionStatus;

    private static final Set<MatchPeriod> PLAYABLE_PERIODS = EnumSet.of(
            MatchPeriod.FIRST_HALF,
            MatchPeriod.SECOND_HALF,
            MatchPeriod.EXTRA_TIME_FIRST_HALF,
            MatchPeriod.EXTRA_TIME_SECOND_HALF,
            MatchPeriod.PENALTIES);

    public MatchDTO update(Long id, MatchRequest request) {
        validateForUpdate(request);
        Match match = resolver.resolveMatch(id);

        MatchStatus previousStatus = match.getStatus();
        MatchPeriod previousPeriod = match.getPeriod();

        boolean isStartingMatch = request.getStatus() == MatchStatus.LIVE
                && match.getStatus() == MatchStatus.SCHEDULED;

        if (isStartingMatch) {
            verifyLineupsExist(match);
            if (match.getStartedAt() == null) {
                Instant now = Instant.now();
                match.setStartedAt(now);
                match.setPeriodStartedAt(now);
                match.setPeriod(MatchPeriod.FIRST_HALF);
                recordPlayedPeriod(match, MatchPeriod.FIRST_HALF);
            }
        }

        if (request.getPeriod() != null && match.getPeriod() != request.getPeriod()) {
            validatePeriodProgression(match, request.getPeriod());
            if (isPlayablePeriod(request.getPeriod())) {
                match.setPeriodStartedAt(Instant.now());
            }
        }
        applyUpdatableFields(request, match);
        finalizeTieBreakerIfNeeded(match, request);

        Match updated = matchRepository.save(match);

        // A knockout match just finished: if it completed its round, the next round
        // is generated (or the competition is marked COMPLETED after the final).
        if (previousStatus != MatchStatus.FINISHED && updated.getStatus() == MatchStatus.FINISHED) {
            advanceKnockoutStage.advance(updated.getId());
        }

        // Competition goes ONGOING when its first match starts, and COMPLETED when
        // the last league match (or the cup final) finishes.
        if (previousStatus != updated.getStatus()) {
            updateCompetitionStatus.sync(updated.getId());
        }

        if (previousStatus != updated.getStatus() || previousPeriod != updated.getPeriod()) {
            matchBroadcastService.broadcastState(updated);
        }

        return MatchDTO.fromEntity(updated);
    }

    private void applyUpdatableFields(MatchRequest request, Match match) {
        if (request.getHomeTeamId() != null) {
            match.setHomeTeam(resolver.resolveTeam(request.getHomeTeamId()));
        }
        if (request.getAwayTeamId() != null) {
            match.setAwayTeam(resolver.resolveTeam(request.getAwayTeamId()));
        }
        if (request.getCompetitionId() != null) {
            applyCompetition(match, request.getCompetitionId());
        }
        if (request.getStadium() != null) {
            match.setStadium(request.getStadium());
        }
        if (request.getGround() != null) {
            match.setGround(request.getGround());
        }
        if (request.getMatchDate() != null) {
            match.setMatchDate(request.getMatchDate());
        }
        if (request.getMatchType() != null) {
            match.setMatchType(request.getMatchType());
        }
        if (request.getStatus() != null) {
            match.setStatus(request.getStatus());
        }
        if (request.getPeriod() != null) {
            recordPlayedPeriod(match, request.getPeriod());
            match.setPeriod(request.getPeriod());
        }
    }

    // ---------- Validation ----------

    private void validateForUpdate(MatchRequest request) {
        if (request.getHomeTeamId() != null
                && request.getAwayTeamId() != null
                && request.getHomeTeamId().equals(request.getAwayTeamId())) {
            throw new IllegalArgumentException("homeTeamId and awayTeamId cannot be the same team");
        }
    }

    private void validatePeriodProgression(Match match, MatchPeriod targetPeriod) {
        if (match.getPeriod() == MatchPeriod.PRE_MATCH && isAdvancedPeriod(targetPeriod)) {
            throw new IllegalStateException(
                    "Cannot start advanced match period. The match must play [FIRST_HALF] first.");
        }
    }

    private void recordPlayedPeriod(Match match, MatchPeriod period) {
        if (isPlayablePeriod(period) && !match.getMatchPeriods().contains(period)) {
            match.getMatchPeriods().add(period);
        }
    }

    private boolean isPlayablePeriod(MatchPeriod period) {
        return PLAYABLE_PERIODS.contains(period);
    }

    private boolean isAdvancedPeriod(MatchPeriod period) {
        return period == MatchPeriod.SECOND_HALF ||
                period == MatchPeriod.EXTRA_TIME_FIRST_HALF ||
                period == MatchPeriod.EXTRA_TIME_SECOND_HALF;
    }

    private void verifyLineupsExist(Match match) {
        boolean hasBothLineups = match.getHomeLineup() != null && match.getAwayLineup() != null;
        if (!hasBothLineups) {
            throw new BadRequestException("Cannot start match. Missing required lineups");
        }
    }

    private void applyCompetition(Match match, Long competitionId) {
        if (competitionId == null) {
            match.setCompetition(null);
            return;
        }
        Competition competition = resolver.resolveCompetition(competitionId);
        match.setCompetition(competition);
    }

    /**
     * A knockout match that finishes level needs a tieBreaker recorded —
     * Match#getWinner() throws for a drawn knockout match with
     * tieBreaker == NONE. This runs after applyUpdatableFields so it sees
     * the final status/score/matchType for this request, and only acts
     * when the match is being finished as a level knockout fixture.
     * Non-knockout matches, and knockout matches that didn't end level,
     * are left untouched (tieBreaker stays NONE).
     */
    private void finalizeTieBreakerIfNeeded(Match match, MatchRequest request) {
        if (match.getStatus() != MatchStatus.FINISHED) {
            return;
        }
        if (match.getMatchType() != MatchType.KNOCKOUT && match.getMatchType() != MatchType.FINAL) {
            return;
        }

        List<Match> roundMatches = (match.getCompetition() == null || match.getRound() == null)
                ? List.of()
                : matchRepository.findByCompetitionIdAndRound(match.getCompetition().getId(), match.getRound());

        // Single leg: level scoreline. Leg 1: never. Leg 2: level on AGGREGATE.
        if (!KnockoutTieResolver.needsTieBreaker(match, roundMatches)) {
            return;
        }

        MatchTieBreaker tieBreaker = request.getTieBreaker();
        if (tieBreaker == null || tieBreaker == MatchTieBreaker.NONE) {
            if (match.getTieBreaker() != null && match.getTieBreaker() != MatchTieBreaker.NONE) {
                return; // already recorded on an earlier update
            }
            throw new BadRequestException(
                    "Match " + match.getId() + " is a knockout fixture that is level"
                            + (match.getLeg() != null ? " on aggregate" : "")
                            + " - tieBreaker must be provided to finish it.");
        }
        match.setTieBreaker(tieBreaker);
    }
}