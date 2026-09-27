package com.zestio.app.transfer.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.zestio.app.transfer.enums.TransferType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TransferResponseDTO {
    private Long id;
    private Long playerId;
    private String playerName;
    private Long fromTeamId;
    private String fromTeamName;
    private Long toTeamId;
    private String toTeamName;
    private TransferType transferType;
    private BigDecimal fee;
    private LocalDate transferDate;
}
