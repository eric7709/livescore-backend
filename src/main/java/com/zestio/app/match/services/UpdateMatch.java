package com.zestio.app.match.services;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.zestio.app.competition.Competition;
import com.zestio.app.exceptions.BadRequestException;
import com.zestio.app.match.Match;
import com.zestio.app.match.MatchRepository;
import com.zestio.app.match.dto.MatchDTO;
import com.zestio.app.match.dto.MatchRequest;
import com.zestio.app.match.enums.MatchPeriod;
import com.zestio.app.match.enums.MatchStatus;
import com.zestio.app.match.helpers.MatchBroadcastService;
import com.zestio.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateMatch {
    private final Resolver resolver;
    private final MatchRepository matchRepository;
    private final MatchBroadcastService matchBroadcastService;

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

        Match updated = matchRepository.save(match);

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
}