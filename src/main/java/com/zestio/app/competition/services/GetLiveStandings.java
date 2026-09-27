package com.zestio.app.competition.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.zestio.app.competition.Competition;
import com.zestio.app.competition.dto.TeamStandingDTO;
import com.zestio.app.competition.helpers.BuildTable;
import com.zestio.app.match.Match;
import com.zestio.app.match.MatchRepository;
import com.zestio.app.match.enums.MatchStatus;
import com.zestio.app.utils.Resolver;

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
