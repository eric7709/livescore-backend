package com.zestio.app.match.services;

import org.springframework.stereotype.Service;

import com.zestio.app.competition.Competition;
import com.zestio.app.match.Match;
import com.zestio.app.match.MatchRepository;
import com.zestio.app.match.dto.MatchDTO;
import com.zestio.app.match.dto.MatchRequest;
import com.zestio.app.match.enums.MatchPeriod;
import com.zestio.app.match.enums.MatchStatus;
import com.zestio.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateMatch {
    private final Resolver resolver;
    private final MatchRepository matchRepository;

    public MatchDTO create(MatchRequest request) {
        validateForCreate(request);
        Match match = new Match();
        match.setHomeTeam(resolver.resolveTeam(request.getHomeTeamId()));
        match.setAwayTeam(resolver.resolveTeam(request.getAwayTeamId()));
        applyCompetition(match, request.getCompetitionId());
        if (request.getStadium() != null) {
            match.setStadium(request.getStadium());
        }
        if (request.getMatchDate() != null) {
            match.setMatchDate(request.getMatchDate());
        }
        if (request.getMatchType() != null) {
            match.setMatchType(request.getMatchType());
        }
        if(request.getGround() != null){
            match.setGround(request.getGround());
        }
        match.setStatus(MatchStatus.SCHEDULED);
        match.setPeriod(MatchPeriod.PRE_MATCH);
        match.setStartedAt(null);
        match.setPeriodStartedAt(null);
        Match saved = matchRepository.save(match);
        return MatchDTO.fromEntity(saved);
    }

    private void validateForCreate(MatchRequest request) {
        if (request.getHomeTeamId() == null) {
            throw new IllegalArgumentException("homeTeamId is required");
        }
        if (request.getAwayTeamId() == null) {
            throw new IllegalArgumentException("awayTeamId is required");
        }
        if (request.getHomeTeamId().equals(request.getAwayTeamId())) {
            throw new IllegalArgumentException("homeTeamId and awayTeamId cannot be the same team");
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
