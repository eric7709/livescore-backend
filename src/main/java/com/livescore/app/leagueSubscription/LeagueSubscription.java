package com.livescore.app.leagueSubscription;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.livescore.app.league.League;
import com.livescore.app.leagueSubscription.enums.SubscriptionPlan;
import com.livescore.app.leagueSubscription.enums.SubscriptionStatus;
import com.livescore.app.utils.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "league_subscriptions", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_league_subscription_payment_reference",
                columnNames = "payment_reference"
        )
})
@Getter
@Setter
@NoArgsConstructor
public class LeagueSubscription extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "league_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_league_subscription_league")
    )
    private League league;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriptionStatus status;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(name = "payment_reference", unique = true, length = 150)
    private String paymentReference;
}
