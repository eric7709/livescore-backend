package com.livescore.app.competition.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.livescore.app.competition.Competition;
import com.livescore.app.competition.dto.TeamStandingDTO;
import com.livescore.app.competition.utils.BuildTable;
import com.livescore.app.match.Match;
import com.livescore.app.match.MatchRepository;
import com.livescore.app.match.enums.MatchStatus;
import com.livescore.app.utils.Resolver;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetLiveStandings {
    private final Resolver resolver;
    private final MatchRepository matchRepository;
    private final BuildTable buildTable;

    public List<TeamStandingDTO> get(Long competitionId) {
        Competition competition = resolver.resolveCompetition(competitionId);

        List<Match> relevantMatches = matchRepository
                .findByCompetitionIdAndStatusInOrderByMatchDateAsc(
                        competitionId,
                        List.of(MatchStatus.FINISHED, MatchStatus.LIVE, MatchStatus.SCHEDULED));
        return buildTable.buildTable(competition, relevantMatches, true);
    }

}
