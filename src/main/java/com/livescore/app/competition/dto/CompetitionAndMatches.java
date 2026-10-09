package com.livescore.app.competition.dto;

import java.util.List;

import com.livescore.app.competition.Competition;
import com.livescore.app.match.Match;
import com.livescore.app.match.dto.MatchListDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CompetitionAndMatches {
    private Long competitionId;
    private String competitionName;
    private String competitionCode;
    private String competitionLogoUrl;
    private List<MatchListDTO> matches;

    public static CompetitionAndMatches toDTO(Competition competition) {
        CompetitionAndMatches competitionAndMatches = new CompetitionAndMatches();
        competitionAndMatches.setCompetitionCode(competition.getCompetitionCode());
        competitionAndMatches.setCompetitionId(competition.getId());
        competitionAndMatches.setCompetitionLogoUrl(competition.getLogoUrl());
        competitionAndMatches.setCompetitionName(competition.getName());
        return competitionAndMatches;
    }

    public static CompetitionAndMatches toDTO(Competition competition, List<Match> matches) {
        CompetitionAndMatches competitionAndMatches = new CompetitionAndMatches();
        competitionAndMatches.setCompetitionCode(competition.getCompetitionCode());
        competitionAndMatches.setCompetitionId(competition.getId());
        competitionAndMatches.setCompetitionLogoUrl(competition.getLogoUrl());
        competitionAndMatches.setCompetitionName(competition.getName());
        if (matches.size() > 0) {
            List<MatchListDTO> matchLists = MatchListDTO.allToMatchListDTO(matches);
            competitionAndMatches.setMatches(matchLists);;
        }
        return competitionAndMatches;
    }

}
