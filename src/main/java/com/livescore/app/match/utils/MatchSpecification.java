package com.livescore.app.match.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.livescore.app.competition.Competition;
import com.livescore.app.match.Match;
import com.livescore.app.match.dto.MatchSearchRequest;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public class MatchSpecification {

    public static Specification<Match> search(MatchSearchRequest request) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            if (request.getMatchId() != null) {
                predicates.add(cb.equal(root.get("id"), request.getMatchId()));
            }

            // Single inner join shared by the competition and league filters.
            // Friendlies (no competition) are excluded when either is set.
            if (request.getCompetitionId() != null || request.getLeagueId() != null) {
                Join<Match, Competition> competition = root.join("competition", JoinType.INNER);

                if (request.getCompetitionId() != null) {
                    predicates.add(cb.equal(competition.get("id"), request.getCompetitionId()));
                }
                if (request.getLeagueId() != null) {
                    predicates.add(cb.equal(competition.get("league").get("id"), request.getLeagueId()));
                }
            }

            // Checks if teamId matches either the home team OR the away team
            if (request.getTeamId() != null) {
                Predicate isHome = cb.equal(root.get("homeTeam").get("id"), request.getTeamId());
                Predicate isAway = cb.equal(root.get("awayTeam").get("id"), request.getTeamId());
                predicates.add(cb.or(isHome, isAway));
            }

            if (request.getDate() != null) {
                LocalDateTime startOfDay = request.getDate().atStartOfDay();
                LocalDateTime endOfDay = request.getDate().plusDays(1).atStartOfDay();
                predicates.add(cb.greaterThanOrEqualTo(root.get("matchDate"), startOfDay));
                predicates.add(cb.lessThan(root.get("matchDate"), endOfDay));
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