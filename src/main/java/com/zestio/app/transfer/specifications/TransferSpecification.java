package com.zestio.app.transfer.specifications;

import java.time.LocalDate;

import org.springframework.data.jpa.domain.Specification;

import com.zestio.app.profile.Profile;
import com.zestio.app.team.Team;
import com.zestio.app.transfer.Transfer;
import com.zestio.app.transfer.dtos.TransferFilterDTO;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public final class TransferSpecification {

    private TransferSpecification() {}

    public static Specification<Transfer> withFilters(TransferFilterDTO filter) {
        return (root, query, cb) -> {
            Join<Transfer, Profile> player = root.join("player", JoinType.LEFT);
            Join<Transfer, Team> fromTeam = root.join("from", JoinType.LEFT);
            Join<Transfer, Team> toTeam = root.join("to", JoinType.LEFT);

            Predicate predicate = cb.conjunction();

            if (filter == null) {
                return predicate;
            }

            if (hasText(filter.getSearch())) {
                String search = "%" + filter.getSearch().trim().toLowerCase() + "%";
                Predicate playerName = cb.like(cb.lower(player.get("fullName")), search);
                Predicate firstName = cb.like(cb.lower(player.get("firstName")), search);
                Predicate lastName = cb.like(cb.lower(player.get("lastName")), search);
                Predicate fromName = cb.like(cb.lower(fromTeam.get("name")), search);
                Predicate toName = cb.like(cb.lower(toTeam.get("name")), search);

                Predicate playerId = null;
                try {
                    Long numeric = Long.valueOf(filter.getSearch().trim());
                    playerId = cb.equal(player.get("id"), numeric);
                } catch (NumberFormatException ignored) {
                    // Search is simply treated as text when it is not numeric.
                }

                Predicate textSearch = cb.or(playerName, firstName, lastName, fromName, toName);
                predicate = cb.and(predicate, playerId == null ? textSearch : cb.or(textSearch, playerId));
            }

            if (filter.getPlayerId() != null) {
                predicate = cb.and(predicate, cb.equal(player.get("id"), filter.getPlayerId()));
            }

            if (filter.getFromTeamId() != null) {
                predicate = cb.and(predicate, cb.equal(fromTeam.get("id"), filter.getFromTeamId()));
            }

            if (filter.getToTeamId() != null) {
                predicate = cb.and(predicate, cb.equal(toTeam.get("id"), filter.getToTeamId()));
            }

            if (filter.getTransferType() != null) {
                predicate = cb.and(predicate, cb.equal(root.get("transferType"), filter.getTransferType()));
            }

            LocalDate dateFrom = filter.getDateFrom();
            LocalDate dateTo = filter.getDateTo();

            if (dateFrom != null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("transferDate"), dateFrom));
            }

            if (dateTo != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("transferDate"), dateTo));
            }

            return predicate;
        };
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
