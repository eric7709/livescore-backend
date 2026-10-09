package com.livescore.app.transfer.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.livescore.app.transfer.enums.TransferType;

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
    private Long leagueId;
    private Integer newSquadNumber;
}