package com.zestio.app.match;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.zestio.app.match.dto.CompetitionMatchesDTO;
import com.zestio.app.match.dto.MatchDTO;
import com.zestio.app.match.dto.MatchOverviewDTO;
import com.zestio.app.match.dto.MatchRequest;
import com.zestio.app.match.dto.MatchSearchRequest;
import com.zestio.app.match.enums.MatchStatus;
import com.zestio.app.match.services.CreateMatch;
import com.zestio.app.match.services.DeleteMatch;
import com.zestio.app.match.services.GetAllCompetitionMatches;
import com.zestio.app.match.services.GetAllMatches;
import com.zestio.app.match.services.GetCompetitionMatchesForDate;
import com.zestio.app.match.services.GetLiveMatches;
import com.zestio.app.match.services.GetMatchById;
import com.zestio.app.match.services.GetMatchOverView;
import com.zestio.app.match.services.SearchMatches;
import com.zestio.app.match.services.UpdateMatch;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {
    private final CreateMatch createMatch;
    private final UpdateMatch updateMatch;
    private final GetMatchById getMatchById;
    private final GetAllMatches getAllMatches;
    private final GetMatchOverView getMatchOverView;
    private final SearchMatches searchMatches;
    private final GetLiveMatches getLiveMatches;
    private final DeleteMatch deleteMatch;
    private final GetCompetitionMatchesForDate getCompetitionMatchesForDate;
    private final GetAllCompetitionMatches getAllCompetitionMatches;

    @PostMapping
    public ResponseEntity<MatchDTO> create(@RequestBody MatchRequest request) {
        MatchDTO created = createMatch.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatchDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(getMatchById.get(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<MatchDTO>> search(
            @ModelAttribute MatchSearchRequest request,
            @PageableDefault(sort = "matchDate", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(searchMatches.search(request, pageable));
    }

    @GetMapping
    public ResponseEntity<List<MatchDTO>> getAll() {
        return ResponseEntity.ok(getAllMatches.get());
    }

    @GetMapping("/{matchId}/overview")
    public MatchOverviewDTO getMatchOverview(@PathVariable Long matchId) {
        return getMatchOverView.get(matchId);
    }

    @GetMapping("/{competitionId}/matches/live")
    @ResponseStatus(HttpStatus.OK)
    public List<Long> getLiveMatches(@PathVariable Long competitionId) {
        return getLiveMatches.get(competitionId);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatchDTO> updateMatch(
            @PathVariable Long id,
            @RequestBody MatchRequest request) {
        return ResponseEntity.ok(updateMatch.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMatch(@PathVariable Long id) {
        deleteMatch.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-date")
    public List<CompetitionMatchesDTO> getMatchesByDate(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) List<MatchStatus> status) {
        return getCompetitionMatchesForDate.get(date, status);
    }

    @GetMapping("/all")
    public List<CompetitionMatchesDTO> getAllMatches(
            @RequestParam(required = false) List<MatchStatus> status) {
        return getAllCompetitionMatches.get(status);
    }
}