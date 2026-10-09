package com.livescore.app.auth.utils;

import org.springframework.data.jpa.domain.Specification;

import com.livescore.app.auth.User;
import com.livescore.app.auth.dto.UserQueryParams;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;

public final class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> search(UserQueryParams request) {

        return (root, query, cb) -> {

            Predicate predicate = cb.conjunction();

            if (request == null) {
                return predicate;
            }

            // Free-text search: email OR full name.
            if (hasText(request.getSearch())) {
                String term = "%" + request.getSearch().trim().toLowerCase() + "%";

                // Built from first + last too, so rows created before
                // full_name existed (null) are still found.
                Expression<String> combinedName = cb.lower(
                        cb.concat(cb.concat(root.<String>get("firstName"), " "),
                                root.<String>get("lastName")));

                Predicate matchesSearch = cb.or(
                        cb.like(cb.lower(root.get("email")), term),
                        cb.like(cb.lower(cb.coalesce(root.<String>get("fullName"), "")), term),
                        cb.like(combinedName, term));

                predicate = cb.and(predicate, matchesSearch);
            }

            if (hasText(request.getFirstName())) {
                predicate = cb.and(predicate, cb.like(
                        cb.lower(root.get("firstName")),
                        "%" + request.getFirstName().trim().toLowerCase() + "%"));
            }

            if (hasText(request.getLastName())) {
                predicate = cb.and(predicate, cb.like(
                        cb.lower(root.get("lastName")),
                        "%" + request.getLastName().trim().toLowerCase() + "%"));
            }

            if (hasText(request.getEmail())) {
                predicate = cb.and(predicate, cb.like(
                        cb.lower(root.get("email")),
                        "%" + request.getEmail().trim().toLowerCase() + "%"));
            }

            if (request.getEnabled() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("enabled"), request.getEnabled()));
            }

            if (request.getProfileId() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("profile").get("id"), request.getProfileId()));
            }

            // User.league is a plain @ManyToOne, so filter on it directly.
            if (request.getLeagueId() != null) {
                predicate = cb.and(predicate,
                        cb.equal(root.get("league").get("id"), request.getLeagueId()));
            }

            return predicate;
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}