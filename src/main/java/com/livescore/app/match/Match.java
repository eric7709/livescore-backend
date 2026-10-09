package com.livescore.app.match;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.livescore.app.competition.Competition;
import com.livescore.app.match.enums.MatchGround;
import com.livescore.app.match.enums.MatchPeriod;
import com.livescore.app.match.enums.MatchStatus;
import com.livescore.app.match.enums.MatchTieBreaker;
import com.livescore.app.match.enums.MatchType;
import com.livescore.app.matchEvents.MatchEvent;
import com.livescore.app.matchLineup.MatchLineup;
import com.livescore.app.team.Team;
import com.livescore.app.utils.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
public class Match extends BaseEntity {

    // Match Information
    private String stadium;
    private Instant matchDate;
    private Instant startedAt;
    private Instant periodStartedAt;
    private String abandonedReason;

    // Score
    @Column(nullable = false)
    private Integer homeScore = 0;

    @Column(nullable = false)
    private Integer awayScore = 0;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchPeriod period = MatchPeriod.PRE_MATCH;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status = MatchStatus.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchGround ground = MatchGround.NEUTRAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchType matchType = MatchType.REGULAR;

    private Integer leg;

    /**
     * Which round this match belongs to within its competition. Used to
     * detect when a knockout round has fully finished (all matches sharing
     * the same competition + round are COMPLETED) so the next round can be
     * generated. Set by the fixture generators; null for matches created
     * before this field existed, or where round tracking doesn't apply.
     */
    private Integer round;

    /**
     * How a drawn scoreline was resolved. Stays NONE for matches that don't
     * need a winner (e.g. league fixtures); required for a knockout match
     * that ends level on homeScore/awayScore — see {@link #getWinner()}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchTieBreaker tieBreaker = MatchTieBreaker.NONE;

    @ElementCollection(targetClass = MatchPeriod.class)
    @CollectionTable(name = "match_periods", joinColumns = @JoinColumn(name = "match_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "period")
    private List<MatchPeriod> matchPeriods = new ArrayList<>();

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "home_team_id", nullable = false)
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "away_team_id", nullable = false)
    private Team awayTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competition_id")
    private Competition competition;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchEvent> events = new ArrayList<>();

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MatchLineup> matchLineups = new ArrayList<>();

    /**
     * True when this match isn't attached to any competition - i.e. a
     * friendly. Matches are never tied to a league directly; a friendly is
     * simply a match with no {@link #competition}, and can be played between
     * any two teams regardless of which league each belongs to.
     */
    public boolean isFriendly() {
        return competition == null;
    }

    /**
     * Rounds only make sense within a competition's fixture list - guard
     * against one being set on a friendly by mistake.
     */
    @PrePersist
    @PreUpdate
    private void validateCompetitionContext() {
        if (isFriendly() && round != null) {
            throw new IllegalStateException(
                    "Match " + getId() + " has no competition, so it can't belong to a round");
        }
    }

    public MatchLineup getHomeLineup() {
        return matchLineups.stream()
                .filter(lineup -> lineup.getTeam().getId().equals(homeTeam.getId()))
                .findFirst()
                .orElse(null);
    }

    public MatchLineup getAwayLineup() {
        return matchLineups.stream()
                .filter(lineup -> lineup.getTeam().getId().equals(awayTeam.getId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Replaces the home team's lineup for this match. Removes any existing
     * lineup for the home team (orphanRemoval deletes it) and wires the
     * back-reference on the new lineup so cascading persist/save works from
     * either the Match or the MatchLineup side.
     */
    public void setHomeLineup(MatchLineup lineup) {
        matchLineups.removeIf(existing -> existing.getTeam().getId().equals(homeTeam.getId()));
        if (lineup != null) {
            lineup.setMatch(this);
            matchLineups.add(lineup);
        }
    }

    /**
     * Replaces the away team's lineup for this match. See {@link #setHomeLineup}.
     */
    public void setAwayLineup(MatchLineup lineup) {
        matchLineups.removeIf(existing -> existing.getTeam().getId().equals(awayTeam.getId()));
        if (lineup != null) {
            lineup.setMatch(this);
            matchLineups.add(lineup);
        }
    }

    /**
     * The team that won this match, or null if it's level and unresolved.
     * Uses homeScore/awayScore first; falls back to tieBreaker when they're
     * equal. Throws if the match is drawn and status isn't COMPLETED yet, or
     * if it's drawn with no tieBreaker recorded — callers that need a
     * definite winner (e.g. knockout progression) should let that propagate
     * rather than silently guessing.
     */
    public Team getWinner() {
        if (status != MatchStatus.FINISHED) {
            throw new IllegalStateException("Match " + getId() + " hasn't been completed yet");
        }
        if (!homeScore.equals(awayScore)) {
            return homeScore > awayScore ? homeTeam : awayTeam;
        }
        return switch (tieBreaker) {
            case HOME_WIN_ON_PENALTIES, HOME_WIN_ON_AWAY_GOALS -> homeTeam;
            case AWAY_WIN_ON_PENALTIES, AWAY_WIN_ON_AWAY_GOALS -> awayTeam;
            case NONE -> throw new IllegalStateException(
                    "Match " + getId() + " is drawn " + homeScore + "-" + awayScore
                            + " with no tieBreaker recorded — set one before progressing the knockout round");
        };
    }
}