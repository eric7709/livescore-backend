package com.livescore.app.leagueSubscription;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.livescore.app.leagueSubscription.dto.LeagueSubscriptionDTO;
import com.livescore.app.leagueSubscription.dto.LeagueSubscriptionRequest;
import com.livescore.app.leagueSubscription.dto.LeagueSubscriptionSearchRequest;
import com.livescore.app.leagueSubscription.enums.SubscriptionStatus;
import com.livescore.app.leagueSubscription.services.CreateLeagueSubscription;
import com.livescore.app.leagueSubscription.services.DeleteLeagueSubscription;
import com.livescore.app.leagueSubscription.services.GetLeagueSubscriptionById;
import com.livescore.app.leagueSubscription.services.SearchLeagueSubscriptions;
import com.livescore.app.leagueSubscription.services.UpdateLeagueSubscription;
import com.livescore.app.leagueSubscription.services.UpdateLeagueSubscriptionStatus;
import com.livescore.app.utils.PageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/league-subscriptions")
@RequiredArgsConstructor
public class LeagueSubscriptionController {

    private final CreateLeagueSubscription createLeagueSubscription;
    private final GetLeagueSubscriptionById getLeagueSubscriptionById;
    private final SearchLeagueSubscriptions searchLeagueSubscriptions;
    private final UpdateLeagueSubscription updateLeagueSubscription;
    private final UpdateLeagueSubscriptionStatus updateLeagueSubscriptionStatus;
    private final DeleteLeagueSubscription deleteLeagueSubscription;

    @PostMapping
    public ResponseEntity<LeagueSubscriptionDTO> create(
            @RequestBody LeagueSubscriptionRequest request) {

        LeagueSubscriptionDTO created = createLeagueSubscription.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeagueSubscriptionDTO> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(getLeagueSubscriptionById.get(id));
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<LeagueSubscriptionDTO>> search(
            @ModelAttribute LeagueSubscriptionSearchRequest request,
            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        return ResponseEntity.ok(
                searchLeagueSubscriptions.search(request, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeagueSubscriptionDTO> update(
            @PathVariable Long id,
            @RequestBody LeagueSubscriptionRequest request) {

        return ResponseEntity.ok(
                updateLeagueSubscription.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<LeagueSubscriptionDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam SubscriptionStatus status) {

        return ResponseEntity.ok(
                updateLeagueSubscriptionStatus.update(id, status));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        deleteLeagueSubscription.delete(id);

        return ResponseEntity.noContent().build();
    }
}
