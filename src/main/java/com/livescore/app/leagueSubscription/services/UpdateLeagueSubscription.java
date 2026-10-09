package com.livescore.app.leagueSubscription.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.leagueSubscription.LeagueSubscription;
import com.livescore.app.leagueSubscription.LeagueSubscriptionRepository;
import com.livescore.app.leagueSubscription.dto.LeagueSubscriptionDTO;
import com.livescore.app.leagueSubscription.dto.LeagueSubscriptionRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpdateLeagueSubscription {

    private final LeagueSubscriptionRepository subscriptionRepository;

    @Transactional
    public LeagueSubscriptionDTO update(
            Long id,
            LeagueSubscriptionRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Subscription request is required");
        }

        LeagueSubscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "League subscription not found"));

        if (request.getPlan() != null) {
            subscription.setPlan(request.getPlan());
        }

        if (request.getStatus() != null) {
            subscription.setStatus(request.getStatus());
        }

        if (request.getAmount() != null) {
            if (request.getAmount().signum() < 0) {
                throw new IllegalArgumentException("amount cannot be negative");
            }
            subscription.setAmount(request.getAmount());
        }

        if (request.getStartDate() != null) {
            subscription.setStartDate(request.getStartDate());
        }

        if (request.getEndDate() != null) {
            subscription.setEndDate(request.getEndDate());
        }

        if (subscription.getEndDate().isBefore(subscription.getStartDate())) {
            throw new IllegalArgumentException(
                    "endDate cannot be before startDate");
        }

        if (request.getPaymentReference() != null
                && !request.getPaymentReference().equals(subscription.getPaymentReference())) {

            if (!request.getPaymentReference().isBlank()
                    && subscriptionRepository.existsByPaymentReference(
                            request.getPaymentReference())) {
                throw new IllegalArgumentException("Payment reference already exists");
            }

            subscription.setPaymentReference(request.getPaymentReference());
        }

        return LeagueSubscriptionDTO.fromEntity(
                subscriptionRepository.save(subscription));
    }
}
