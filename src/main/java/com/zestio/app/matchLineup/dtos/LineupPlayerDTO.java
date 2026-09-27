package com.zestio.app.matchLineup.dtos;

import com.zestio.app.matchLineup.LineupPlayer;
import com.zestio.app.matchLineup.enums.LineupStatus;
import com.zestio.app.matchLineup.enums.MissingReason;
import com.zestio.app.profile.enums.Position;

import lombok.Data;

@Data
public class LineupPlayerDTO {
    private Long playerId;
    private String playerName;
    private Integer squadNumber;
    private Position position;
    private String slotLabel;
    private LineupStatus status;
    private MissingReason missingReason;

    public static LineupPlayerDTO mapToDTO(LineupPlayer lineupPlayer) {
        LineupPlayerDTO dto = new LineupPlayerDTO();
        dto.setPlayerId(lineupPlayer.getPlayer().getId());
        dto.setPlayerName(lineupPlayer.getPlayer().getFullName());
        dto.setSquadNumber(lineupPlayer.getPlayer().getSquadNumber());
        dto.setPosition(lineupPlayer.getPosition());
        dto.setSlotLabel(lineupPlayer.getSlotLabel());
        dto.setStatus(lineupPlayer.getStatus());
        dto.setMissingReason(lineupPlayer.getMissingReason());
        return dto;
    }
}