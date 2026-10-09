package com.livescore.app.leagueSubscription;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LeagueSubscriptionRepository
        extends JpaRepository<LeagueSubscription, Long>,
                JpaSpecificationExecutor<LeagueSubscription> {

    Optional<LeagueSubscription> findByPaymentReference(String paymentReference);

    boolean existsByPaymentReference(String paymentReference);
}
