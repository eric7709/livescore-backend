package com.zestio.app.matchLineup;

import java.util.ArrayList;
import java.util.List;

import com.zestio.app.match.Match;
import com.zestio.app.matchLineup.enums.Formation;
import com.zestio.app.profile.Profile;
import com.zestio.app.team.Team;
import com.zestio.app.utils.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "match_id", "team_id" }))
@NoArgsConstructor
public class MatchLineup extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "captain_id")
    private Profile captain;

    @OneToMany(mappedBy = "matchLineup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineupPlayer> players = new ArrayList<>();

    private Formation formation;
}