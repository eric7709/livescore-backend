package com.livescore.app.competition;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.livescore.app.competition.enums.CompetitionLegFormat;
import com.livescore.app.competition.enums.CompetitionScope;
import com.livescore.app.competition.enums.CompetitionStatus;
import com.livescore.app.competition.enums.CompetitionType;
import com.livescore.app.league.League;
import com.livescore.app.match.Match;
import com.livescore.app.team.Team;
import com.livescore.app.utils.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "competitions")
@NoArgsConstructor
@AllArgsConstructor
public class Competition extends BaseEntity {

    private String name;

    @OneToMany(mappedBy = "competition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Match> matches;

    private String competitionCode;

    @ManyToMany
    @JoinTable(name = "competition_teams", joinColumns = @JoinColumn(name = "competition_id"), inverseJoinColumns = @JoinColumn(name = "team_id"))
    private Set<Team> teams = new HashSet<>();

    private String logoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompetitionScope scope = CompetitionScope.LOCAL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompetitionStatus status = CompetitionStatus.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompetitionLegFormat legFormat = CompetitionLegFormat.DOUBLE;

    private Integer totalTeams = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompetitionType competitionType;

    private Integer totalRounds;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_id", nullable = false)
    private League league;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    // -------------------------------------------------------------------------
    // Team Relationship Helpers
    // -------------------------------------------------------------------------

    /**
     * Adds a single team. Set guarantees no duplicate entries.
     */
    public boolean addTeam(Team team) {
        if (team == null) {
            return false;
        }
        boolean added = this.teams.add(team);
        if (added) {
            if (team.getCompetitions() != null) {
                team.getCompetitions().add(this);
            }
            this.totalTeams = this.teams.size();
        }
        return added;
    }

    /**
     * Bulk adds teams while ignoring duplicates.
     */
    public void addTeams(Collection<Team> newTeams) {
        if (newTeams != null) {
            newTeams.forEach(this::addTeam);
        }
    }

    /**
     * Removes a team and updates totalTeams counter.
     */
    public boolean removeTeam(Team team) {
        if (team == null) {
            return false;
        }
        boolean removed = this.teams.remove(team);
        if (removed) {
            if (team.getCompetitions() != null) {
                team.getCompetitions().remove(this);
            }
            this.totalTeams = this.teams.size();
        }
        return removed;
    }

    /**
     * Replaces the teams collection safely.
     */
    public void replaceTeams(Collection<Team> newTeams) {
        this.teams.clear();
        if (newTeams != null) {
            addTeams(newTeams);
        }
        this.totalTeams = this.teams.size();
    }
}