package com.livescore.app.match.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.competition.Competition;
import com.livescore.app.match.Match;
import com.livescore.app.match.MatchRepository;
import com.livescore.app.match.dto.HeadToHeadEntryDTO;
import com.livescore.app.match.dto.MatchOverviewDTO;
import com.livescore.app.match.dto.TeamFormEntryDTO;
import com.livescore.app.match.enums.MatchResult;
import com.livescore.app.match.enums.MatchStatus;
import com.livescore.app.team.Team;
import com.livescore.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetMatchOverView {
    private final MatchRepository matchRepository;
    private final Resolver resolver;

    @Transactional(readOnly = true)
    public MatchOverviewDTO get(Long matchId) {
        Match match = resolver.resolveMatch(matchId);
        Team homeTeam = match.getHomeTeam();
        Team awayTeam = match.getAwayTeam();
        Pageable lastFive = PageRequest.of(0, 5);
        List<TeamFormEntryDTO> homeForm = matchRepository
                .findRecentFinishedByTeam(homeTeam.getId(), MatchStatus.FINISHED, lastFive)
                .stream()
                .map(m -> toFormEntry(m, homeTeam.getId()))
                .collect(Collectors.toList());

        List<TeamFormEntryDTO> awayForm = matchRepository
                .findRecentFinishedByTeam(awayTeam.getId(), MatchStatus.FINISHED, lastFive)
                .stream()
                .map(m -> toFormEntry(m, awayTeam.getId()))
                .collect(Collectors.toList());

        List<HeadToHeadEntryDTO> headToHead = matchRepository
                .findHeadToHead(homeTeam.getId(), awayTeam.getId(), MatchStatus.FINISHED, lastFive)
                .stream()
                .map(this::toHeadToHeadEntry)
                .collect(Collectors.toList());

        return new MatchOverviewDTO(homeForm, awayForm, headToHead);
    }

    private TeamFormEntryDTO toFormEntry(Match match, Long anchorTeamId) {
        boolean anchorIsHome = match.getHomeTeam().getId().equals(anchorTeamId);
        Competition competition = match.getCompetition();
        return new TeamFormEntryDTO(
                match.getId(),
                match.getHomeTeam().getId(),
                match.getHomeTeam().getName(),
                match.getHomeTeam().getLogoUrl(),
                match.getAwayTeam().getId(),
                match.getAwayTeam().getName(),
                match.getAwayTeam().getLogoUrl(),
                match.getHomeScore(),
                match.getAwayScore(),
                match.getHomeScore() + "-" + match.getAwayScore(),
                competition != null ? competition.getId() : null,
                competition != null ? competition.getName() : null,
                competition != null ? competition.getLogoUrl() : null,
                match.getMatchDate(),
                resolveResult(match, anchorIsHome));
    }

    private HeadToHeadEntryDTO toHeadToHeadEntry(Match match) {
        Competition competition = match.getCompetition();
        return new HeadToHeadEntryDTO(
                match.getId(),
                match.getHomeTeam().getId(),
                match.getHomeTeam().getName(),
                match.getHomeTeam().getLogoUrl(),
                match.getAwayTeam().getId(),
                match.getAwayTeam().getName(),
                match.getAwayTeam().getLogoUrl(),
                match.getHomeScore(),
                match.getAwayScore(),
                match.getHomeScore() + "-" + match.getAwayScore(),
                competition != null ? competition.getId() : null,
                competition != null ? competition.getName() : null,
                competition != null ? competition.getLogoUrl() : null,
                match.getMatchDate());
    }

    private MatchResult resolveResult(Match match, boolean anchorIsHome) {
        if (match.getHomeScore().equals(match.getAwayScore())) {
            return MatchResult.DRAW;
        }
        boolean homeWon = match.getHomeScore() > match.getAwayScore();
        boolean anchorWon = anchorIsHome == homeWon;
        return anchorWon ? MatchResult.WIN : MatchResult.LOSS;
    }

}
