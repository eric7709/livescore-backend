package com.livescore.app.profile;

import java.time.LocalDate;

import com.livescore.app.auth.enums.Role;
import com.livescore.app.league.League;
import com.livescore.app.profile.enums.CaptainStatus;
import com.livescore.app.profile.enums.PlayerStatus;
import com.livescore.app.profile.enums.Position;
import com.livescore.app.profile.enums.PreferredFoot;
import com.livescore.app.team.Team;
import com.livescore.app.utils.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "profile")
@Getter
@Setter
public class Profile extends BaseEntity {

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    // Optional: staff invited via email/password (see CreateModerator) get a
    // Profile with no phone number until they add one. Still unique when
    // present; Postgres allows multiple NULLs in a unique column.
    @Column(unique = true)
    private String phoneNumber;

    private String fullName;

    private String avatarUrl;

    private Integer squadNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerStatus status = PlayerStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "league_id", nullable = false, foreignKey = @ForeignKey(name = "fk_profile_league"))
    private League league;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Position position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CaptainStatus captainStatus = CaptainStatus.NONE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private PreferredFoot preferredFoot;

    @Column(nullable = true)
    private Integer height;

    @Column(nullable = true)
    private LocalDate dateOfBirth;

    @PrePersist
    @PreUpdate
    private void updateFullName() {
        if (firstName != null && lastName != null) {
            this.fullName = firstName + " " + lastName;
        }
    }

    public String getFullName() {
        if (fullName == null && firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }

        return fullName;
    }
}