package com.zestio.app.match.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.zestio.app.match.Match;
import com.zestio.app.match.enums.MatchGround;
import com.zestio.app.match.enums.MatchStatus;
import com.zestio.app.match.enums.MatchType;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MatchListDTO {
    private Long id;
    private Integer homeScore;
    private Integer awayScore;
    private MatchStatus status;
    private MatchType matchType;
    private String abandonedReason;
    private Instant matchDate;
    private Instant startedAt;
    private Long homeTeamId;
    private MatchGround ground;
    private String homeTeamName;
    private String homeTeamLogoUrl;
    private String homeTeamCode;
    private Long awayTeamId;
    private String awayTeamName;
    private String awayTeamLogoUrl;
    private String awayTeamCode;

    public static MatchListDTO toMatchListDTO(Match match) {
        MatchListDTO matchListDTO = new MatchListDTO();
        matchListDTO.setAbandonedReason(match.getAbandonedReason());
        matchListDTO.setAwayScore(match.getAwayScore());
        matchListDTO.setAwayTeamCode(match.getAwayTeam().getTeamCode());
        matchListDTO.setAwayTeamId(match.getAwayTeam().getId());
        matchListDTO.setAwayTeamLogoUrl(match.getAwayTeam().getLogoUrl());
        matchListDTO.setAwayTeamName(match.getAwayTeam().getName());
        matchListDTO.setGround(match.getGround());
        matchListDTO.setHomeScore(match.getHomeScore());
        matchListDTO.setHomeTeamCode(match.getHomeTeam().getTeamCode());
        matchListDTO.setHomeTeamId(match.getHomeTeam().getId());
        matchListDTO.setHomeTeamLogoUrl(match.getHomeTeam().getLogoUrl());
        matchListDTO.setHomeTeamName(match.getHomeTeam().getName());
        matchListDTO.setId(match.getId());
        matchListDTO.setMatchDate(match.getMatchDate());
        matchListDTO.setMatchType(match.getMatchType());
        matchListDTO.setStartedAt(match.getStartedAt());
        matchListDTO.setStatus(match.getStatus());
        return matchListDTO;
    }
    
    public static List<MatchListDTO> allToMatchListDTO(List<Match> matches){
        List<MatchListDTO> matchLists = new ArrayList<>();
        for(Match match : matches){
            matchLists.add(toMatchListDTO(match));
        }
        return matchLists;
    }
}
