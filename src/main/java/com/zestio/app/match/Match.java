package com.zestio.app.match;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.zestio.app.competition.Competition;
import com.zestio.app.match.enums.MatchGround;
import com.zestio.app.match.enums.MatchPeriod;
import com.zestio.app.match.enums.MatchStatus;
import com.zestio.app.match.enums.MatchType;
import com.zestio.app.matchEvents.MatchEvent;
import com.zestio.app.matchLineup.MatchLineup;
import com.zestio.app.team.Team;
import com.zestio.app.utils.BaseEntity;

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
    private MatchGround ground = MatchGround.LOCATION;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchType matchType = MatchType.REGULAR;

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
}