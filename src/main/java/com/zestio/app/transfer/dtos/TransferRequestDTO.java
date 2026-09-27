package com.zestio.app.transfer.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.zestio.app.transfer.enums.TransferType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferRequestDTO {
    private Long playerId;
    private Long toTeamId;
    private TransferType transferType;
    private BigDecimal fee;
    private LocalDate transferDate;

    // Optional. Only used when the player's current squad number is already
    // taken on the destination team — the client resends the request with
    // this set to the number the user chose instead.
    private Integer newSquadNumber;
}