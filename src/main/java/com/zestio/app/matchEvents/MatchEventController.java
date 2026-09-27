package com.zestio.app.matchEvents;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.zestio.app.matchEvents.dtos.GetMatchSummaries;
import com.zestio.app.matchEvents.dtos.MatchEventDTO;
import com.zestio.app.matchEvents.dtos.MatchEventRequest;
import com.zestio.app.matchEvents.dtos.MatchStatistic;
import com.zestio.app.matchEvents.dtos.MatchSummary;
import com.zestio.app.matchEvents.services.CreateMatchEvent;
import com.zestio.app.matchEvents.services.DeleteMatchEvent;
import com.zestio.app.matchEvents.services.GetMatchEvent;
import com.zestio.app.matchEvents.services.GetMatchStatistics;

import java.util.List;

@RestController
@RequestMapping("/api/match-events")
@RequiredArgsConstructor
public class MatchEventController {

    private final CreateMatchEvent createMatchEvent;
    private final GetMatchStatistics getMatchStatistics;
    private final GetMatchEvent getMatchEvent;
    private final DeleteMatchEvent deleteMatchEvent;
    private final GetMatchSummaries getMatchSummaries;

    @PostMapping
    public ResponseEntity<MatchEventDTO> create(@RequestBody MatchEventRequest request) {
        MatchEventDTO created = createMatchEvent.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<MatchEventDTO>> getMatchEvent(@PathVariable Long id) {
        return ResponseEntity.ok(getMatchEvent.get(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteMatchEvent.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{matchId}/statistics")
    @ResponseStatus(HttpStatus.OK)
    public List<MatchStatistic> getMatchStatistics(@PathVariable Long matchId) {
        return getMatchStatistics.getMatchStatistics(matchId);
    }

    @GetMapping("/{matchId}/summary")
    public ResponseEntity<List<MatchSummary>> getSummaries(@PathVariable Long matchId) {
        return ResponseEntity.ok(getMatchSummaries.getMatchSummaries(matchId));
    }
}