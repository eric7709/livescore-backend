package com.zestio.app.match.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import com.zestio.app.match.Match;
import com.zestio.app.match.enums.MatchPeriod;
import com.zestio.app.match.enums.MatchStatus;
import com.zestio.app.match.enums.MatchType;

import lombok.Data;

@Data
public class MatchSummary {
    private Long id;
    private Integer homeScore;
    private Integer awayScore;
    private MatchPeriod period;
    private MatchStatus status;
    private String abandonedReason;
    private String stadium;
    private Instant matchDate;
    private Instant startedAt;
    private Instant periodStartedAt;
    private MatchType matchType;
    private Long homeTeamId;
    private String homeTeamName;
    private Long awayTeamId;
    private String awayTeamName;
    private Long competitionId;
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Maps a single Match entity to a MatchSummary DTO.
     */
    public static MatchSummary fromEntity(Match match) {
        if (match == null) {
            return null;
        }

        MatchSummary summary = new MatchSummary();
        summary.setId(match.getId());
        summary.setHomeScore(match.getHomeScore());
        summary.setAwayScore(match.getAwayScore());
        summary.setPeriod(match.getPeriod());
        summary.setStatus(match.getStatus());
        summary.setAbandonedReason(match.getAbandonedReason());
        summary.setStadium(match.getStadium());
        summary.setMatchDate(match.getMatchDate());
        summary.setStartedAt(match.getStartedAt());
        summary.setPeriodStartedAt(match.getPeriodStartedAt());
        summary.setMatchType(match.getMatchType());

        // Extract IDs safely from relationships
        summary.setHomeTeamId(match.getHomeTeam() != null ? match.getHomeTeam().getId() : null);
        summary.setHomeTeamName(match.getHomeTeam() != null ? match.getHomeTeam().getName() : null);
        summary.setAwayTeamId(match.getAwayTeam() != null ? match.getAwayTeam().getId() : null);
        summary.setAwayTeamName(match.getAwayTeam() != null ? match.getAwayTeam().getName() : null);
        summary.setCompetitionId(match.getCompetition() != null ? match.getCompetition().getId() : null);

        // Inherited fields from BaseEntity
        summary.setCreatedAt(match.getCreatedAt());
        summary.setUpdatedAt(match.getUpdatedAt());

        return summary;
    }

    /**
     * Maps a list of Match entities to a list of MatchSummary DTOs.
     */
    public static List<MatchSummary> fromEntities(List<Match> matches) {
        if (matches == null || matches.isEmpty()) {
            return Collections.emptyList();
        }

        return matches.stream()
                .map(MatchSummary::fromEntity)
                .toList();
    }
}