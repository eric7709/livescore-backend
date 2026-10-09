package com.livescore.app.leagueSubscription.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.league.League;
import com.livescore.app.league.LeagueRepository;
import com.livescore.app.leagueSubscription.LeagueSubscription;
import com.livescore.app.leagueSubscription.LeagueSubscriptionRepository;
import com.livescore.app.leagueSubscription.dto.LeagueSubscriptionDTO;
import com.livescore.app.leagueSubscription.dto.LeagueSubscriptionRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CreateLeagueSubscription {

    private final LeagueSubscriptionRepository subscriptionRepository;
    private final LeagueRepository leagueRepository;

    @Transactional
    public LeagueSubscriptionDTO create(LeagueSubscriptionRequest request) {
        validateForCreate(request);

        League league = leagueRepository.findById(request.getLeagueId())
                .orElseThrow(() -> new IllegalArgumentException("League not found"));

        if (request.getPaymentReference() != null
                && !request.getPaymentReference().isBlank()
                && subscriptionRepository.existsByPaymentReference(request.getPaymentReference())) {
            throw new IllegalArgumentException("Payment reference already exists");
        }

        LeagueSubscription subscription = new LeagueSubscription();
        subscription.setLeague(league);
        subscription.setPlan(request.getPlan());
        subscription.setStatus(request.getStatus());
        subscription.setAmount(request.getAmount());
        subscription.setStartDate(request.getStartDate());
        subscription.setEndDate(request.getEndDate());
        subscription.setPaymentReference(request.getPaymentReference());

        return LeagueSubscriptionDTO.fromEntity(
                subscriptionRepository.save(subscription));
    }

    private void validateForCreate(LeagueSubscriptionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Subscription request is required");
        }
        if (request.getLeagueId() == null) {
            throw new IllegalArgumentException("leagueId is required");
        }
        if (request.getPlan() == null) {
            throw new IllegalArgumentException("plan is required");
        }
        if (request.getStatus() == null) {
            throw new IllegalArgumentException("status is required");
        }
        if (request.getAmount() == null) {
            throw new IllegalArgumentException("amount is required");
        }
        if (request.getAmount().signum() < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }
        if (request.getStartDate() == null) {
            throw new IllegalArgumentException("startDate is required");
        }
        if (request.getEndDate() == null) {
            throw new IllegalArgumentException("endDate is required");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("endDate cannot be before startDate");
        }
    }
}
