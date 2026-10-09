package com.livescore.app.team.dto;

import java.util.ArrayList;
import java.util.List;

import com.livescore.app.profile.Profile;
import com.livescore.app.team.Team;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TeamResponseDTO {
    private Long id;
    private String name;
    private String logoUrl;
    private Long leagueId;
    private String leagueName;
    private Long managerId;
    private String managerName;
    private String stadium;
    private String teamCode;

    public static TeamResponseDTO toDTO(Team team) {
        if (team == null)
            return null;

        TeamResponseDTO dto = new TeamResponseDTO();

        dto.setId(team.getId());
        dto.setName(team.getName());
        dto.setLogoUrl(team.getLogoUrl());
        dto.setTeamCode(team.getTeamCode());
        if (team.getStadium() != null) {
            dto.setStadium(team.getStadium());
        }
        if (team.getLeague() != null) {
            dto.setLeagueId(team.getLeague().getId());
            dto.setLeagueName(team.getLeague().getName());
        }
        if (team.getManager() != null) {
            Profile manager = team.getManager();
            dto.setManagerId(manager.getId());
            dto.setManagerName(manager.getFirstName() + " " + manager.getLastName());
        }
        return dto;
    }

    public static List<TeamResponseDTO> allToDTO(List<Team> teams) {
        List<TeamResponseDTO> teamResponses = new ArrayList<>();
        for (Team team : teams) {
            teamResponses.add(toDTO(team));
        }
        return teamResponses;
    }

    // DTO → ENTITY
    public static Team toEntity(TeamResponseDTO dto) {
        if (dto == null)
            return null;
        Team team = new Team();
        team.setName(dto.getName());
        team.setLogoUrl(dto.getLogoUrl());
        team.setTeamCode(dto.getTeamCode());
        return team;
    }
}