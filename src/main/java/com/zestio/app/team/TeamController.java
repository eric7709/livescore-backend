package com.zestio.app.team;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.zestio.app.profile.enums.PlayerStatus;
import com.zestio.app.profile.enums.Position;
import com.zestio.app.team.dto.Fixture;
import com.zestio.app.team.dto.Result;
import com.zestio.app.team.dto.TeamRequestDTO;
import com.zestio.app.team.dto.TeamResponseDTO;
import com.zestio.app.team.dto.TeamSquadAndManager;
import com.zestio.app.team.dto.TeamSummaryDTO;
import com.zestio.app.team.services.CreateTeam;
import com.zestio.app.team.services.CreateTeams;
import com.zestio.app.team.services.DeleteTeam;
import com.zestio.app.team.services.GetAllTeams;
import com.zestio.app.team.services.GetTeamOpponent;
import com.zestio.app.team.services.GetTeam;
import com.zestio.app.team.services.GetTeamFixtures;
import com.zestio.app.team.services.GetTeamResult;
import com.zestio.app.team.services.GetTeamSquadAndManager;
import com.zestio.app.team.services.GetTeamSquadNumbers;
import com.zestio.app.team.services.GetTeamsByIds;
import com.zestio.app.team.services.SearchTeam;
import com.zestio.app.team.services.UpdateTeam;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final GetTeamFixtures getTeamFixtures;
    private final GetTeamResult getTeamResult;
    private final GetTeamSquadAndManager getTeamSquadAndManager;
    private final GetTeamSquadNumbers getTeamSquadNumbers;
    private final CreateTeam createTeam;
    private final GetTeamOpponent getTeamOpponent;
    private final UpdateTeam updateTeam;
    private final CreateTeams createTeams;
    private final GetTeam getTeam;
    private final DeleteTeam deleteTeam;
    private final GetTeamsByIds getTeamsByIds;
    private final GetAllTeams getAllTeams;
    private final SearchTeam searchTeam;

    @GetMapping("/{teamId}")
    @ResponseStatus(HttpStatus.OK)
    public TeamResponseDTO getTeamById(@PathVariable Long teamId) {
        return TeamResponseDTO.toDTO(getTeam.get(teamId));
    }

    @GetMapping("/{teamId}/squad-numbers")
    @ResponseStatus(HttpStatus.OK)
    public List<Integer> getTeamSquadNumbers(@PathVariable Long teamId) {
        return getTeamSquadNumbers.get(teamId);
    }

    @GetMapping("/{teamId}/match/{matchId}/opponent")
    @ResponseStatus(HttpStatus.OK)
    public TeamResponseDTO getOpponentById(@PathVariable Long teamId, @PathVariable Long matchId) {
        return TeamResponseDTO.toDTO(getTeamOpponent.get(teamId, matchId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public TeamResponseDTO createTeam(@RequestBody TeamRequestDTO dto) {
        return TeamResponseDTO.toDTO(createTeam.create(dto));
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.OK)
    public List<TeamResponseDTO> createTeams(@RequestBody List<TeamRequestDTO> dtos) {
        return createTeams.create(dtos)
                .stream()
                .map(TeamResponseDTO::toDTO)
                .toList();
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.OK)
    public List<TeamResponseDTO> getTeamsByIds(@RequestBody List<Long> ids) {
        return getTeamsByIds.get(ids)
                .stream()
                .map(TeamResponseDTO::toDTO)
                .toList();
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public Page<TeamResponseDTO> search(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return searchTeam.search(query, PageRequest.of(page, size))
                .map(TeamResponseDTO::toDTO);
    }

    @GetMapping("/summary")
    @ResponseStatus(HttpStatus.OK)
    public List<TeamSummaryDTO> getTeamSummary(
            @RequestParam(required = false) String query) {
        List<Team> teams;
        if (query != null && !query.isEmpty()) {
            teams = searchTeam.search(query);
        } else {
            teams = getAllTeams.get();
        }
        return TeamSummaryDTO.allToDTO(teams);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public TeamResponseDTO update(
            @PathVariable Long id,
            @RequestBody TeamRequestDTO dto) {
        return TeamResponseDTO.toDTO(updateTeam.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable Long id) {
        deleteTeam.delete(id);
    }

    @GetMapping("/{teamId}/fixtures")
    public Page<Fixture> getTeamFixtures(
            @PathVariable Long teamId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Long competitionId,
            @PageableDefault(size = 10) Pageable pageable) {
        return getTeamFixtures.get(teamId, date, competitionId, pageable);
    }

    @GetMapping("/{teamId}/results")
    public Page<Result> getTeamResults(
            @PathVariable Long teamId,
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) Long competitionId,
            @PageableDefault(size = 10) Pageable pageable) {
        return getTeamResult.get(teamId, date, competitionId, pageable);
    }

    @GetMapping("/{teamId}/squad")
    public TeamSquadAndManager getTeamSquad(
            @PathVariable Long teamId,
            @RequestParam(required = false) PlayerStatus status,
            @RequestParam(required = false) List<Position> position) {
        return getTeamSquadAndManager.get(teamId, status, position);
    }

}