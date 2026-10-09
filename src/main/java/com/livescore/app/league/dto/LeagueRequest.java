package com.livescore.app.league.dto;

import com.livescore.app.leagueSubscription.enums.SubscriptionPlan;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeagueRequest {

    private String name;

    private String slug;

    private String description;

    private String logoUrl;

    private SubscriptionPlan subscriptionPlan;
}