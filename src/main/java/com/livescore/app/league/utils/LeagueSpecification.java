package com.livescore.app.league.utils;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.Specification;

import com.livescore.app.league.League;
import com.livescore.app.leagueSubscription.enums.SubscriptionPlan;
import com.livescore.app.leagueSubscription.enums.SubscriptionStatus;

public final class LeagueSpecification {

    private LeagueSpecification() {
    }

    public static Specification<League> query(String query) {

        return (root, criteriaQuery, criteriaBuilder) -> {

            if (query == null || query.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String value = "%" + query.trim().toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("name")),
                            value
                    ),

                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("slug")),
                            value
                    ),

                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("description")),
                            value
                    )
            );
        };
    }

    public static Specification<League> name(String name) {

        return (root, criteriaQuery, criteriaBuilder) -> {

            if (name == null || name.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.trim().toLowerCase() + "%"
            );
        };
    }

    public static Specification<League> slug(String slug) {

        return (root, criteriaQuery, criteriaBuilder) -> {

            if (slug == null || slug.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("slug")),
                    "%" + slug.trim().toLowerCase() + "%"
            );
        };
    }

    public static Specification<League> subscriptionPlan(
            SubscriptionPlan subscriptionPlan) {

        return (root, criteriaQuery, criteriaBuilder) -> {

            if (subscriptionPlan == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("subscriptionPlan"),
                    subscriptionPlan
            );
        };
    }

    public static Specification<League> subscriptionStatus(
            SubscriptionStatus subscriptionStatus) {

        return (root, criteriaQuery, criteriaBuilder) -> {

            if (subscriptionStatus == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("subscriptionStatus"),
                    subscriptionStatus
            );
        };
    }

    public static Specification<League> active(Boolean active) {

        return (root, criteriaQuery, criteriaBuilder) -> {

            if (active == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("active"),
                    active
            );
        };
    }

    public static Specification<League> createdFrom(
            LocalDateTime createdFrom) {

        return (root, criteriaQuery, criteriaBuilder) -> {

            if (createdFrom == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("createdAt"),
                    createdFrom
            );
        };
    }

    public static Specification<League> createdTo(
            LocalDateTime createdTo) {

        return (root, criteriaQuery, criteriaBuilder) -> {

            if (createdTo == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("createdAt"),
                    createdTo
            );
        };
    }
}