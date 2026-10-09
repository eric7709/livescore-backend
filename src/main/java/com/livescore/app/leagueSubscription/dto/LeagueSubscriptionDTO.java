package com.livescore.app.leagueSubscription.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import com.livescore.app.leagueSubscription.LeagueSubscription;
import com.livescore.app.leagueSubscription.enums.SubscriptionPlan;
import com.livescore.app.leagueSubscription.enums.SubscriptionStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeagueSubscriptionDTO {

    private Long id;

    private Long leagueId;

    private String leagueName;

    private SubscriptionPlan plan;

    private SubscriptionStatus status;

    private BigDecimal amount;

    private LocalDate startDate;

    private LocalDate endDate;

    private String paymentReference;

    private Instant createdAt;

    private Instant updatedAt;

    public static LeagueSubscriptionDTO fromEntity(LeagueSubscription subscription) {
        return LeagueSubscriptionDTO.builder()
                .id(subscription.getId())
                .leagueId(subscription.getLeague() != null ? subscription.getLeague().getId() : null)
                .leagueName(subscription.getLeague() != null ? subscription.getLeague().getName() : null)
                .plan(subscription.getPlan())
                .status(subscription.getStatus())
                .amount(subscription.getAmount())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .paymentReference(subscription.getPaymentReference())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }
}
