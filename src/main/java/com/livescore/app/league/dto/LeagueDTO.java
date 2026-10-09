package com.livescore.app.league.dto;

import java.time.LocalDateTime;

import com.livescore.app.league.League;
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
public class LeagueDTO {

    private Long id;

    private String name;

    private String slug;

    private String description;

    private String logoUrl;

    private SubscriptionPlan subscriptionPlan;

    private SubscriptionStatus subscriptionStatus;

    private boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static LeagueDTO fromEntity(League league) {

        return LeagueDTO.builder()
                .id(league.getId())
                .name(league.getName())
                .slug(league.getSlug())
                .description(league.getDescription())
                .logoUrl(league.getLogoUrl())
                .subscriptionPlan(league.getSubscriptionPlan())
                .subscriptionStatus(league.getSubscriptionStatus())
                .active(league.isActive())
                .build();
    }
}