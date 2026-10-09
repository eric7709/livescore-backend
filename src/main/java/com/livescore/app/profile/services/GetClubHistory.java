package com.livescore.app.profile.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.profile.dto.ClubHistoryResponseDTO;
import com.livescore.app.team.Team;
import com.livescore.app.transfer.Transfer;
import com.livescore.app.transfer.TransferRepository;
import com.livescore.app.transfer.enums.TransferType;

import lombok.RequiredArgsConstructor;

/**
 * Builds a player's club-history table (Real Madrid / Flamengo style rows)
 * from their transfers, newest stint first.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetClubHistory {

    private static final BigDecimal ONE_MILLION = BigDecimal.valueOf(1_000_000);

    private final TransferRepository transferRepository;

    public List<ClubHistoryResponseDTO> get(Long playerId) {

        List<Transfer> transfers = transferRepository.findByPlayerId(playerId).stream()
                .sorted(Comparator.comparing(transfer -> transfer.getTransferDate()))
                .toList();

        List<ClubHistoryResponseDTO> history = new ArrayList<>();

        for (int i = 0; i < transfers.size(); i++) {
            Transfer current = transfers.get(i);
            Transfer next = (i + 1 < transfers.size()) ? transfers.get(i + 1) : null;

            history.add(toDto(current, next));
        }

        Collections.reverse(history);
        return history;
    }

    private ClubHistoryResponseDTO toDto(Transfer transfer, Transfer nextTransfer) {

        TransferType type = resolveType(transfer);

        String fee = null;
        String loanFeeLabel = null;

        if (type == TransferType.LOAN) {
            loanFeeLabel = "Loan fee: " + (transfer.getFee() != null ? formatFee(transfer.getFee()) : "N/A");
        } else if (transfer.getFee() != null) {
            fee = formatFee(transfer.getFee());
        }

        Team fromClub = transfer.getFrom();
        Team toClub = transfer.getTo();

        return ClubHistoryResponseDTO.builder()
                .id(transfer.getId())
                .fromClubId(fromClub != null ? fromClub.getId() : null)
                .fromClubName(fromClub != null ? fromClub.getName() : null)
                .fromClubLogoUrl(fromClub != null ? fromClub.getLogoUrl() : null)
                .toClubId(toClub != null ? toClub.getId() : null)
                .toClubName(toClub != null ? toClub.getName() : null)
                .toClubLogoUrl(toClub != null ? toClub.getLogoUrl() : null)
                .from(transfer.getTransferDate())
                .to(nextTransfer != null ? nextTransfer.getTransferDate() : null)
                .fee(fee)
                .loanFeeLabel(loanFeeLabel)
                .type(type)
                .build();
    }

    private TransferType resolveType(Transfer transfer) {
        if (transfer.getTransferType().equals(TransferType.LOAN)) {
            return TransferType.LOAN;
        }
        if (transfer.getFee() == null) {
            return TransferType.FREE;
        }
        return TransferType.PERMANENT;
    }

    private String formatFee(BigDecimal fee) {
        BigDecimal millions = fee.divide(ONE_MILLION, 1, RoundingMode.HALF_UP);
        return "€" + millions.toPlainString() + "M";
    }
}