package com.livescore.app.profile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.livescore.app.auth.enums.Role;
import com.livescore.app.profile.dto.AddRosterMemberRequest;
import com.livescore.app.profile.dto.ClubHistoryResponseDTO;
import com.livescore.app.profile.dto.CompetitionStatResponseDTO;
import com.livescore.app.profile.dto.ProfileQueryParams;
import com.livescore.app.profile.dto.ProfileRequestDTO;
import com.livescore.app.profile.dto.ProfileResponseDTO;
import com.livescore.app.profile.dto.ProfileStatsDTO;
import com.livescore.app.profile.dto.ProfileSummaryResponseDTO;
import com.livescore.app.profile.enums.CaptainStatus;
import com.livescore.app.profile.enums.PlayerStatus;
import com.livescore.app.profile.enums.Position;
import com.livescore.app.profile.enums.PreferredFoot;
import com.livescore.app.profile.utils.ProfileMapper;
import com.livescore.app.profile.services.AddRosterMember;
import com.livescore.app.profile.services.CreateProfile;
import com.livescore.app.profile.services.CreateProfiles;
import com.livescore.app.profile.services.DeleteProfile;
import com.livescore.app.profile.services.GetClubHistory;
import com.livescore.app.profile.services.GetCompetitionStats;
import com.livescore.app.profile.services.GetPlayerSummariesByTeam;
import com.livescore.app.profile.services.GetProfile;
import com.livescore.app.profile.services.GetProfileStats;
import com.livescore.app.profile.services.GetProfilesByTeam;
import com.livescore.app.profile.services.MakeCaptain;
import com.livescore.app.profile.services.MakeViceCaptain;
import com.livescore.app.profile.services.SearchProfile;
import com.livescore.app.profile.services.UpdatePlayerStatus;
import com.livescore.app.profile.services.UpdateProfile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileMapper profileMapper;

    private final CreateProfile createProfile;
    private final CreateProfiles createProfiles;
    private final UpdateProfile updateProfile;
    private final DeleteProfile deleteProfile;
    private final UpdatePlayerStatus updatePlayerStatus;
    private final MakeCaptain makeCaptain;
    private final MakeViceCaptain makeViceCaptain;

    private final GetProfile getProfile;
    private final GetProfilesByTeam getProfilesByTeam;
    private final GetPlayerSummariesByTeam getPlayerSummariesByTeam;
    private final SearchProfile searchProfile;

    private final GetProfileStats getProfileStats;
    private final GetClubHistory getClubHistory;
    private final GetCompetitionStats getCompetitionStats;
    private final AddRosterMember addRosterMember;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponseDTO create(
            @RequestBody ProfileRequestDTO dto) {

        return profileMapper.toDTO(
                createProfile.create(dto)
        );
    }

    @GetMapping("/{id}/transfer-history")
    public List<ClubHistoryResponseDTO> getClubHistory(
            @PathVariable("id") Long playerId) {

        return getClubHistory.get(playerId);
    }

    @GetMapping("/{id}/competition-stats")
    public List<CompetitionStatResponseDTO> getCompetitionStats(
            @PathVariable("id") Long playerId) {

        return getCompetitionStats.get(playerId);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ProfileResponseDTO> createAll(
            @RequestBody List<ProfileRequestDTO> dtos) {

        return createProfiles.create(dtos)
                .stream()
                .map(profileMapper::toDTO)
                .toList();
    }

    @GetMapping
    public Page<ProfileResponseDTO> search(
            @RequestParam(required = false) Long leagueId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String phoneNumber,
            @RequestParam(required = false) Integer squadNumber,
            @RequestParam(required = false) PlayerStatus status,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Position position,
            @RequestParam(required = false) CaptainStatus captainStatus,
            @RequestParam(required = false) PreferredFoot preferredFoot,
            @RequestParam(required = false) Integer minHeight,
            @RequestParam(required = false) Integer maxHeight,
            @RequestParam(required = false) LocalDate dateOfBirthFrom,
            @RequestParam(required = false) LocalDate dateOfBirthTo,
            Pageable pageable) {

        ProfileQueryParams params = new ProfileQueryParams();

        params.setLeagueId(leagueId);
        params.setTeamId(teamId);
        params.setFirstName(firstName);
        params.setLastName(lastName);
        params.setFullName(fullName);
        params.setPhoneNumber(phoneNumber);
        params.setSquadNumber(squadNumber);
        params.setStatus(status);
        params.setRole(role);
        params.setPosition(position);
        params.setCaptainStatus(captainStatus);
        params.setPreferredFoot(preferredFoot);
        params.setMinHeight(minHeight);
        params.setMaxHeight(maxHeight);
        params.setDateOfBirthFrom(dateOfBirthFrom);
        params.setDateOfBirthTo(dateOfBirthTo);

        return searchProfile
                .search(params, pageable)
                .map(profileMapper::toDTO);
    }

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public ProfileStatsDTO getStats() {
        return getProfileStats.get();
    }

    @GetMapping("/summary/team/{teamId}")
    public List<ProfileSummaryResponseDTO> getPlayerSummariesByTeam(
            @PathVariable Long teamId) {

        return getPlayerSummariesByTeam.get(teamId);
    }

    @GetMapping("/{id}")
    public ProfileResponseDTO getOne(
            @PathVariable Long id) {

        return profileMapper.toDTO(
                getProfile.get(id)
        );
    }

    @GetMapping("/team/{teamId}")
    public List<ProfileResponseDTO> getByTeamId(
            @PathVariable Long teamId) {

        return getProfilesByTeam.get(teamId)
                .stream()
                .map(profileMapper::toDTO)
                .toList();
    }

    @PostMapping("/roster")
    @ResponseStatus(HttpStatus.CREATED)
    public void addToRoster(
            @Valid @RequestBody AddRosterMemberRequest request) {

        addRosterMember.add(request);
    }

    @PutMapping("/{id}")
    public ProfileResponseDTO update(
            @PathVariable Long id,
            @RequestBody ProfileRequestDTO dto) {

        return profileMapper.toDTO(
                updateProfile.update(id, dto)
        );
    }

    @PatchMapping("/{id}/status")
    public ProfileResponseDTO updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, PlayerStatus> body) {

        return profileMapper.toDTO(
                updatePlayerStatus.update(
                        id,
                        body.get("status")
                )
        );
    }

    @PatchMapping("/{id}/captain")
    public ProfileResponseDTO makeCaptain(
            @PathVariable Long id) {

        return profileMapper.toDTO(
                makeCaptain.make(id)
        );
    }

    @PatchMapping("/{id}/vice-captain")
    public ProfileResponseDTO makeViceCaptain(
            @PathVariable Long id) {

        return profileMapper.toDTO(
                makeViceCaptain.make(id)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id) {

        deleteProfile.delete(id);
    }
}