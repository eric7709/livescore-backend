package com.livescore.app.team;

import com.livescore.app.competition.Competition;
import com.livescore.app.league.League;
import com.livescore.app.profile.Profile;
import com.livescore.app.utils.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Team extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    private String logoUrl;

    @Column(nullable = false)
    private String teamCode;

    private String stadium;

    // Every team belongs to exactly one league
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_id", nullable = false, foreignKey = @ForeignKey(name = "fk_team_league"))
    private League league;

    // Manager is just ONE profile (no cascade chaos)
    @OneToOne
    @JoinColumn(name = "manager_id")
    private Profile manager;

    @ManyToMany(mappedBy = "teams")
    private Set<Competition> competitions = new HashSet<>();

    @OneToMany(mappedBy = "team")
    private List<Profile> players;

}