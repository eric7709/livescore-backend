package com.livescore.app.leagueSubscription.dto;

import java.time.LocalDate;

import com.livescore.app.leagueSubscription.enums.SubscriptionPlan;
import com.livescore.app.leagueSubscription.enums.SubscriptionStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeagueSubscriptionSearchRequest {

    private String query;

    private Long leagueId;

    private SubscriptionPlan plan;

    private SubscriptionStatus status;

    private LocalDate startDateFrom;

    private LocalDate startDateTo;

    private LocalDate endDateFrom;

    private LocalDate endDateTo;
}
