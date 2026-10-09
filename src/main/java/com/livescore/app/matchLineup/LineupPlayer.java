package com.livescore.app.matchLineup;

import com.livescore.app.matchLineup.enums.LineupStatus;
import com.livescore.app.matchLineup.enums.MissingReason;
import com.livescore.app.profile.Profile;
import com.livescore.app.profile.enums.Position;
import com.livescore.app.utils.BaseEntity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "lineup_players")
@Getter
@Setter
public class LineupPlayer extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_lineup_id", nullable = false)
    private MatchLineup matchLineup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Profile player;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LineupStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Position position;

    // Exact tactical slot within the formation (e.g. "LCM", "RCM", "GK").
    // Nullable: rows saved before this change won't have one, and bench /
    // missing players don't occupy a formation slot at all.
    @Column(name = "slot_label")
    private String slotLabel;

    @Enumerated(EnumType.STRING)
    @Column(name = "missing_reason")
    private MissingReason missingReason;
}