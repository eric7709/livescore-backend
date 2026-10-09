    package com.livescore.app.auth.tempUserAndPasswordToken;

    import java.time.LocalDateTime;

    import com.livescore.app.auth.enums.Role;
    import com.livescore.app.league.League;
    import com.livescore.app.team.Team;
    import com.livescore.app.utils.BaseEntity;

    import jakarta.persistence.Column;
    import jakarta.persistence.Entity;
    import jakarta.persistence.EnumType;
    import jakarta.persistence.Enumerated;
    import jakarta.persistence.FetchType;
    import jakarta.persistence.JoinColumn;
    import jakarta.persistence.ManyToOne;
    import jakarta.persistence.PrePersist;
    import jakarta.persistence.Table;
    import jakarta.persistence.UniqueConstraint;
    import lombok.AllArgsConstructor;
    import lombok.Builder;
    import lombok.Getter;
    import lombok.NoArgsConstructor;
    import lombok.Setter;

    @Entity
    @Table(name = "temp_users", uniqueConstraints = {
            @UniqueConstraint(name = "uk_temp_user_email", columnNames = "email"),
            @UniqueConstraint(name = "uk_temp_user_token", columnNames = "token")
    })
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public class TempUser extends BaseEntity {

        @Column(nullable = false, length = 100)
        private String firstName;

        @Column(nullable = false, length = 100)
        private String lastName;

        @Column(nullable = false, length = 150)
        private String email;

        /** Random, unguessable value used in the invite link instead of the row id. */
        @Column(nullable = false, length = 64)
        private String token;

        @Enumerated(EnumType.STRING)
        @Column(nullable = false, length = 30)
        private Role role;

        @Column(nullable = false)
        @Builder.Default
        private boolean enabled = true;

        @Column(nullable = false)
        private LocalDateTime expiresAt;

        @ManyToOne(fetch = FetchType.LAZY, optional = true)
        @JoinColumn(name = "league_id", nullable = true)
        private League league;

        @ManyToOne(fetch = FetchType.LAZY, optional = true)
        @JoinColumn(name = "team_id", nullable = true)
        private Team team;

        @PrePersist
        private void setExpiry() {
            if (expiresAt == null) {
                expiresAt = LocalDateTime.now().plusHours(24);
            }
        }

        public boolean isExpired() {
            return expiresAt != null && !LocalDateTime.now().isBefore(expiresAt);
        }
    }