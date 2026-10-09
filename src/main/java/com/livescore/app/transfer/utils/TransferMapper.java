package com.livescore.app.transfer.utils;

import java.util.List;

import org.springframework.stereotype.Component;

import com.livescore.app.league.League;
import com.livescore.app.profile.Profile;
import com.livescore.app.team.Team;
import com.livescore.app.transfer.Transfer;
import com.livescore.app.transfer.dtos.TransferResponseDTO;

@Component
public class TransferMapper {

    public TransferResponseDTO toDTO(Transfer transfer) {
        if (transfer == null) return null;

        Profile player = transfer.getPlayer();
        Team fromTeam = transfer.getFrom();
        Team toTeam = transfer.getTo();
        // A transfer lives in the player's league (profile.league is non-null).
        League league = player != null ? player.getLeague() : null;

        return TransferResponseDTO.builder()
                .id(transfer.getId())
                .leagueId(league != null ? league.getId() : null)
                .playerId(player != null ? player.getId() : null)
                .playerName(player != null ? player.getFullName() : null)
                .fromTeamId(fromTeam != null ? fromTeam.getId() : null)
                .fromTeamName(fromTeam != null ? fromTeam.getName() : null)
                .toTeamId(toTeam != null ? toTeam.getId() : null)
                .toTeamName(toTeam != null ? toTeam.getName() : null)
                .transferType(transfer.getTransferType())
                .fee(transfer.getFee())
                .transferDate(transfer.getTransferDate())
                .build();
    }

    public List<TransferResponseDTO> toDTOList(List<Transfer> transfers) {
        return transfers.stream().map(this::toDTO).toList();
    }
}