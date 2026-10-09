package com.livescore.app.league;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.livescore.app.league.dto.LeagueDTO;
import com.livescore.app.league.dto.LeagueRequest;
import com.livescore.app.league.dto.LeagueSearchRequest;
import com.livescore.app.league.services.CreateLeague;
import com.livescore.app.league.services.DeleteLeague;
import com.livescore.app.league.services.GetLeagueById;
import com.livescore.app.league.services.SearchLeagues;
import com.livescore.app.league.services.UpdateLeague;
import com.livescore.app.league.services.UpdateLeagueStatus;
import com.livescore.app.utils.PageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/leagues")
@RequiredArgsConstructor
public class LeagueController {

    private final CreateLeague createLeague;
    private final GetLeagueById getLeagueById;
    private final SearchLeagues searchLeagues;
    private final UpdateLeague updateLeague;
    private final DeleteLeague deleteLeague;
    private final UpdateLeagueStatus updateLeagueStatus;

    @PostMapping
    public ResponseEntity<LeagueDTO> create(
            @RequestBody LeagueRequest request) {

        LeagueDTO created = createLeague.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LeagueDTO> getById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                getLeagueById.get(id)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<PageResponse<LeagueDTO>> search(
            @ModelAttribute LeagueSearchRequest request,

            @PageableDefault(
                    size = 20,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable) {

        return ResponseEntity.ok(
                searchLeagues.search(
                        request,
                        pageable
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<LeagueDTO> update(
            @PathVariable Long id,
            @RequestBody LeagueRequest request) {

        return ResponseEntity.ok(
                updateLeague.update(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {

        deleteLeague.delete(id);

        return ResponseEntity.noContent().build();
    }

   

    @PatchMapping("/{id}/status")
    public ResponseEntity<LeagueDTO> updateStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {

        return ResponseEntity.ok(
                updateLeagueStatus.update(id, active)
        );
    }
}