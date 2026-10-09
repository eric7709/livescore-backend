package com.livescore.app.matchEvents;

import com.livescore.app.match.Match;
import com.livescore.app.match.enums.MatchPeriod;
import com.livescore.app.matchEvents.enums.EventType;
import com.livescore.app.profile.Profile;
import com.livescore.app.team.Team;
import com.livescore.app.utils.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;


@Entity
@Table(name = "match_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MatchEvent extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    // Team that the primaryPlayer belongs to / team that did the action
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // 1. PRIMARY PLAYER - who did the action
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_player_id")
    private Profile primaryPlayer;

    // 2. SECONDARY PLAYER - involved with the action
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secondary_player_id")
    private Profile secondaryPlayer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchPeriod period;

    // `minute` and `second` are reserved words in H2 (and most SQL dialects,
    // since they're datetime extraction functions) — backtick-quoting the
    // column name tells Hibernate to escape it for whatever dialect is
    // active, instead of emitting it bare and breaking query parsing.
    @Column(name = "`minute`", nullable = false)
    private Integer minute;

    @Column(name = "`second`", nullable = false)
    private Integer second;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "event_data", columnDefinition = "jsonb")
    private String eventData; // x,y coords, shot xG, etc
}