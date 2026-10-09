package com.livescore.app.league.dto;

import java.time.LocalDateTime;

import com.livescore.app.leagueSubscription.enums.SubscriptionPlan;
import com.livescore.app.leagueSubscription.enums.SubscriptionStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeagueSearchRequest {

    private String query;

    private String name;

    private String slug;

    private SubscriptionPlan subscriptionPlan;

    private SubscriptionStatus subscriptionStatus;

    private Boolean active;

    private LocalDateTime createdFrom;

    private LocalDateTime createdTo;
}