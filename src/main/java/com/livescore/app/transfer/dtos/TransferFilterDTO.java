package com.livescore.app.transfer.dtos;

import java.time.LocalDate;

import com.livescore.app.transfer.enums.TransferType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransferFilterDTO {
    private String search;
    private Long playerId;
    private Long fromTeamId;
    private Long toTeamId;
    private TransferType transferType;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private Long leagueId;
}
