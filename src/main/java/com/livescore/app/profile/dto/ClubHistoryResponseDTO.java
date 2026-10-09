package com.livescore.app.profile.dto;

import java.time.LocalDate;

import com.livescore.app.transfer.enums.TransferType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ClubHistoryResponseDTO {
    private Long id;

    private Long fromClubId; // null for a free-agent signing / no prior club
    private String fromClubName;
    private String fromClubLogoUrl;

    private Long toClubId;
    private String toClubName;
    private String toClubLogoUrl;

    private LocalDate from;
    private LocalDate to; // null = current club
    private String fee; // preformatted, e.g. "€45.0M"; null if not applicable
    private String loanFeeLabel; // e.g. "Loan fee: N/A"; null if not applicable
    private TransferType type;
    private Integer appearances;
}