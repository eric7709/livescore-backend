package com.livescore.app.competition.utils;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.livescore.app.competition.Competition;
import com.livescore.app.competition.dto.CompetitionQueryParams;
import com.livescore.app.match.Match;
import com.livescore.app.team.Team;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public class CompetitionSpecification {

    public static Specification<Competition> search(CompetitionQueryParams request) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Add distinct to avoid duplicates when joining
            query.distinct(true);

            // Search by leagueId
            if (request.getLeagueId() != null) {
                predicates.add(
                        cb.equal(
                                root.get("league").get("id"),
                                request.getLeagueId()
                        )
                );
            }

            // Search by teamId - join with teams
            if (request.getTeamId() != null) {

                Join<Competition, Team> teamJoin =
                        root.join("teams", JoinType.INNER);

                predicates.add(
                        cb.equal(
                                teamJoin.get("id"),
                                request.getTeamId()
                        )
                );
            }

            // Search by matchId - join with matches
            if (request.getMatchId() != null) {

                Join<Competition, Match> matchJoin =
                        root.join("matches", JoinType.INNER);

                predicates.add(
                        cb.equal(
                                matchJoin.get("id"),
                                request.getMatchId()
                        )
                );
            }

            // Search by competitionCode
            if (request.getCompetitionCode() != null
                    && !request.getCompetitionCode().isEmpty()) {

                predicates.add(
                        cb.equal(
                                root.get("competitionCode"),
                                request.getCompetitionCode()
                        )
                );
            }

            // Search by name
            if (request.getName() != null
                    && !request.getName().isEmpty()) {

                predicates.add(
                        cb.like(
                                cb.lower(root.get("name")),
                                "%" + request.getName().toLowerCase() + "%"
                        )
                );
            }

            // Search by status
            if (request.getStatus() != null) {

                predicates.add(
                        cb.equal(
                                root.get("status"),
                                request.getStatus()
                        )
                );
            }

            // Search by scope
            if (request.getScope() != null) {

                predicates.add(
                        cb.equal(
                                root.get("scope"),
                                request.getScope()
                        )
                );
            }

            // Search by legFormat
            if (request.getLegFormat() != null) {

                predicates.add(
                        cb.equal(
                                root.get("legFormat"),
                                request.getLegFormat()
                        )
                );
            }

            // Search by competitionType
            if (request.getCompetitionType() != null) {

                predicates.add(
                        cb.equal(
                                root.get("competitionType"),
                                request.getCompetitionType()
                        )
                );
            }

            // Search by startDate
            if (request.getStartDate() != null) {

                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("startDate"),
                                request.getStartDate().atStartOfDay()
                        )
                );
            }

            // Search by endDate
            if (request.getEndDate() != null) {

                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("endDate"),
                                request.getEndDate().atTime(23, 59, 59)
                        )
                );
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}