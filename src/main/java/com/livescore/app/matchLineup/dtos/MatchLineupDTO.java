package com.livescore.app.matchLineup.dtos;

import java.util.List;

import com.livescore.app.matchLineup.MatchLineup;
import com.livescore.app.matchLineup.enums.Formation;

import lombok.Data;

@Data
public class MatchLineupDTO {
    private Long matchId;
    private Long teamId;
    private String teamName;
    private Long captainId;
    private Formation formation;
    private List<LineupPlayerDTO> lineup;
    public static MatchLineupDTO mapToDTO(MatchLineup lineup) {
        MatchLineupDTO matchLineupDTO = new MatchLineupDTO();
        matchLineupDTO.setCaptainId(lineup.getCaptain().getId());
        matchLineupDTO.setFormation(lineup.getFormation());
        matchLineupDTO.setMatchId(lineup.getMatch().getId());
        matchLineupDTO.setTeamId(lineup.getTeam().getId());
        matchLineupDTO.setTeamName(lineup.getTeam().getName());
        matchLineupDTO.setLineup(lineup.getPlayers().stream().map(LineupPlayerDTO::mapToDTO).toList());
        return matchLineupDTO;
    }
}
