package com.zestio.app.match.helpers;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.zestio.app.match.Match;
import com.zestio.app.match.dto.MatchSearchRequest;

import jakarta.persistence.criteria.Predicate;

public class MatchSpecification {

    public static Specification<Match> search(MatchSearchRequest request) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (request.getMatchId() != null) {
                predicates.add(cb.equal(root.get("id"), request.getMatchId()));
            }
            if (request.getCompetitionId() != null) {
                predicates.add(cb.equal(root.get("competition").get("id"), request.getCompetitionId()));
            }

            // Checks if teamId matches either the home team OR the away team
            if (request.getTeamId() != null) {
                Predicate isHome = cb.equal(root.get("homeTeam").get("id"), request.getTeamId());
                Predicate isAway = cb.equal(root.get("awayTeam").get("id"), request.getTeamId());
                predicates.add(cb.or(isHome, isAway));
            }

            if (request.getStadium() != null) {
                predicates.add(cb.equal(root.get("stadium"), request.getStadium()));
            }
            if (request.getDateFrom() != null) {
                Instant fromInstant = request.getDateFrom().atStartOfDay(ZoneOffset.UTC).toInstant();
                predicates.add(cb.greaterThanOrEqualTo(root.get("matchDate"), fromInstant));
            }
            if (request.getDateTo() != null) {
                Instant toInstant = request.getDateTo().plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
                predicates.add(cb.lessThan(root.get("matchDate"), toInstant));
            }
            if (request.getPeriod() != null) {
                predicates.add(cb.equal(root.get("period"), request.getPeriod()));
            }
            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }
            if (request.getMatchType() != null) {
                predicates.add(cb.equal(root.get("matchType"), request.getMatchType()));
            }

            // Order by matchDate ascending (chronological order: 12th before 14th)
            query.orderBy(cb.asc(root.get("matchDate")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}