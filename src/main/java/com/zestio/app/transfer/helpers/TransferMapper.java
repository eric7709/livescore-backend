package com.zestio.app.transfer.helpers;

import java.util.List;

import org.springframework.stereotype.Component;

import com.zestio.app.profile.Profile;
import com.zestio.app.team.Team;
import com.zestio.app.transfer.Transfer;
import com.zestio.app.transfer.dtos.TransferResponseDTO;

@Component
public class TransferMapper {

    public TransferResponseDTO toDTO(Transfer transfer) {
        if (transfer == null) return null;

        Profile player = transfer.getPlayer();
        Team fromTeam = transfer.getFrom();
        Team toTeam = transfer.getTo();

        return TransferResponseDTO.builder()
                .id(transfer.getId())
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
