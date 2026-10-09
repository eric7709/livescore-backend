package com.livescore.app.team.dto;

import java.util.ArrayList;
import java.util.List;

import com.livescore.app.team.Team;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamSummaryDTO {
    private Long teamId;
    private String teamName;
    private String teamCode;
    private String teamLogoUrl;
    private String teamStadium;

    public static TeamSummaryDTO toDTO(Team team) {
        TeamSummaryDTO teamSummaryDTO = new TeamSummaryDTO();
        teamSummaryDTO.setTeamCode(team.getTeamCode());
        teamSummaryDTO.setTeamLogoUrl(team.getLogoUrl());
        teamSummaryDTO.setTeamName(team.getName());
        teamSummaryDTO.setTeamId(team.getId());
        if (team.getStadium() != null) {
            teamSummaryDTO.setTeamStadium(team.getStadium());
        }
        return teamSummaryDTO;
    }

    public static List<TeamSummaryDTO> allToDTO(List<Team> teams) {
        List<TeamSummaryDTO> list = new ArrayList<>();
        for (Team team : teams) {
            list.add(toDTO(team));
        }
        return list;
    }
}
