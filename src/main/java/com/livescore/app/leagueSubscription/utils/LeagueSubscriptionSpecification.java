package com.livescore.app.leagueSubscription.utils;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.livescore.app.leagueSubscription.LeagueSubscription;
import com.livescore.app.leagueSubscription.enums.SubscriptionPlan;
import com.livescore.app.leagueSubscription.enums.SubscriptionStatus;

public final class LeagueSubscriptionSpecification {

    private LeagueSubscriptionSpecification() {
    }

    public static Specification<LeagueSubscription> query(String query) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (query == null || query.isBlank()) {
                return criteriaBuilder.conjunction();
            }

            String value = "%" + query.trim().toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.join("league").get("name")),
                            value
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.join("league").get("slug")),
                            value
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("paymentReference")),
                            value
                    )
            );
        };
    }

    public static Specification<LeagueSubscription> leagueId(Long leagueId) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (leagueId == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(
                    root.get("league").get("id"),
                    leagueId
            );
        };
    }

    public static Specification<LeagueSubscription> plan(SubscriptionPlan plan) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (plan == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("plan"), plan);
        };
    }

    public static Specification<LeagueSubscription> status(SubscriptionStatus status) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (status == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.equal(root.get("status"), status);
        };
    }

    public static Specification<LeagueSubscription> startDateFrom(LocalDate date) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("startDate"),
                    date
            );
        };
    }

    public static Specification<LeagueSubscription> startDateTo(LocalDate date) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("startDate"),
                    date
            );
        };
    }

    public static Specification<LeagueSubscription> endDateFrom(LocalDate date) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.greaterThanOrEqualTo(
                    root.get("endDate"),
                    date
            );
        };
    }

    public static Specification<LeagueSubscription> endDateTo(LocalDate date) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (date == null) {
                return criteriaBuilder.conjunction();
            }

            return criteriaBuilder.lessThanOrEqualTo(
                    root.get("endDate"),
                    date
            );
        };
    }
}
