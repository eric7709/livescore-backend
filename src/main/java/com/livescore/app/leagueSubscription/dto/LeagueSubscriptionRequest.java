package com.livescore.app.leagueSubscription.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.livescore.app.leagueSubscription.enums.SubscriptionPlan;
import com.livescore.app.leagueSubscription.enums.SubscriptionStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeagueSubscriptionRequest {

    private Long leagueId;

    private SubscriptionPlan plan;

    private SubscriptionStatus status;

    private BigDecimal amount;

    private LocalDate startDate;

    private LocalDate endDate;

    private String paymentReference;
}
