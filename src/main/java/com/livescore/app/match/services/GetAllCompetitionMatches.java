package com.livescore.app.match.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.livescore.app.competition.Competition;
import com.livescore.app.competition.dto.CompetitionSummary;
import com.livescore.app.match.Match;
import com.livescore.app.match.MatchRepository;
import com.livescore.app.match.dto.CompetitionMatchesDTO;
import com.livescore.app.match.dto.MatchSummary;
import com.livescore.app.match.enums.MatchStatus;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetAllCompetitionMatches {

    private final MatchRepository matchRepository;

    /**
     * Fetches all matches in the database (optionally filtered by status)
     * and groups them by competition into CompetitionMatchesDTO entries.
     */
    public List<CompetitionMatchesDTO> get(List<MatchStatus> statuses) {
        List<Match> matches = (statuses == null || statuses.isEmpty())
                ? matchRepository.findAllWithCompetition()
                : matchRepository.findByStatusInWithCompetition(statuses);
        return groupMatchesByCompetition(matches);
    }

    private List<CompetitionMatchesDTO> groupMatchesByCompetition(List<Match> matches) {
        Map<Long, CompetitionMatchesDTO> grouped = new LinkedHashMap<>();

        for (Match match : matches) {
            Competition competition = match.getCompetition();
            if (competition == null) {
                continue;
            }

            CompetitionMatchesDTO dto = grouped.computeIfAbsent(
                    competition.getId(),
                    id -> CompetitionMatchesDTO.builder()
                            .competition(CompetitionSummary.fromEntity(competition))
                            .matches(new ArrayList<>())
                            .build()
            );
            dto.getMatches().add(MatchSummary.fromEntity(match));
        }

        return new ArrayList<>(grouped.values());
    }
}