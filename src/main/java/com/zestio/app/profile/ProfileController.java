package com.zestio.app.profile;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.zestio.app.profile.dto.AddRosterMemberRequest;
import com.zestio.app.profile.dto.ClubHistoryResponseDTO;
import com.zestio.app.profile.dto.CompetitionStatResponseDTO;
import com.zestio.app.profile.dto.ProfileRequestDTO;
import com.zestio.app.profile.dto.ProfileResponseDTO;
import com.zestio.app.profile.dto.ProfileStatsDTO;
import com.zestio.app.profile.dto.ProfileSummaryResponseDTO;
import com.zestio.app.profile.enums.PlayerStatus;
import com.zestio.app.profile.enums.Position;
import com.zestio.app.profile.enums.Role;
import com.zestio.app.profile.helpers.ProfileMapper;
import com.zestio.app.profile.services.AddRosterMember;
import com.zestio.app.profile.services.CreateProfile;
import com.zestio.app.profile.services.CreateProfiles;
import com.zestio.app.profile.services.DeleteProfile;
import com.zestio.app.profile.services.GetClubHistory;
import com.zestio.app.profile.services.GetCompetitionStats;
import com.zestio.app.profile.services.GetFilteredProfiles;
import com.zestio.app.profile.services.GetPlayerSummariesByTeam;
import com.zestio.app.profile.services.GetProfile;
import com.zestio.app.profile.services.GetProfileStats;
import com.zestio.app.profile.services.GetProfilesByTeam;
import com.zestio.app.profile.services.MakeCaptain;
import com.zestio.app.profile.services.MakeViceCaptain;
import com.zestio.app.profile.services.UpdatePlayerStatus;
import com.zestio.app.profile.services.UpdateProfile;

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
    private final GetFilteredProfiles getFilteredProfiles;
    private final GetProfileStats getProfileStats;
    private final GetClubHistory getClubHistory;
    private final GetCompetitionStats getCompetitionStats;

    private final AddRosterMember addRosterMember;

    // ✅ Create profile – 201 Created
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfileResponseDTO create(@RequestBody ProfileRequestDTO dto) {
        return profileMapper.toDTO(createProfile.create(dto));
    }

    @GetMapping("/{id}/transfer-history")
    public List<ClubHistoryResponseDTO> getClubHistory(@PathVariable("id") Long playerId) {
        return getClubHistory.get(playerId);
    }

    @GetMapping("/{id}/competition-stats")
    public List<CompetitionStatResponseDTO> getCompetitionStats(@PathVariable("id") Long playerId) {
        return getCompetitionStats.get(playerId);
    }

    // 🔥 Bulk create profiles – 201 Created
    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ProfileResponseDTO> createAll(@RequestBody List<ProfileRequestDTO> dtos) {
        return createProfiles.create(dtos)
                .stream()
                .map(profile -> profileMapper.toDTO(profile))
                .toList();
    }

    // ✅ Get all profiles – 200 OK (default)
    @GetMapping
    public Page<ProfileResponseDTO> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Position position,
            @RequestParam(required = false) Integer squadNumber,
            @RequestParam(required = false) Long teamId,
            Pageable pageable) {
        Page<Profile> page = getFilteredProfiles.get(search, role, position, squadNumber, teamId, pageable);
        return page.map(profileMapper::toDTO);
    }

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public ProfileStatsDTO getStats() {
        return getProfileStats.get();
    }

    // ✅ Get profile summaries by team – 200 OK
    @GetMapping("/summary/team/{teamId}")
    public List<ProfileSummaryResponseDTO> getPlayerSummariesByTeam(@PathVariable Long teamId) {
        return getPlayerSummariesByTeam.get(teamId);
    }

    // ✅ Get one profile – 200 OK
    @GetMapping("/{id}")
    public ProfileResponseDTO getOne(@PathVariable Long id) {
        return profileMapper.toDTO(getProfile.get(id));
    }

    // ⚽ Get profiles by team ID – 200 OK
    @GetMapping("/team/{teamId}")
    public List<ProfileResponseDTO> getByTeamId(@PathVariable Long teamId) {
        return getProfilesByTeam.get(teamId)
                .stream()
                .map(profile -> profileMapper.toDTO(profile))
                .toList();
    }

    @PostMapping("/roster")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public void addToRoster(@Valid @RequestBody AddRosterMemberRequest request) {
        addRosterMember.add(request);
    }

    // ✅ Update profile – 200 OK (default)
    @PutMapping("/{id}")
    public ProfileResponseDTO update(
            @PathVariable Long id,
            @RequestBody ProfileRequestDTO dto) {
        return profileMapper.toDTO(updateProfile.update(id, dto));
    }

    // 🩹 Update player status – 200 OK
    @PatchMapping("/{id}/status")
    public ProfileResponseDTO updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, PlayerStatus> body) {
        return profileMapper.toDTO(updatePlayerStatus.update(id, body.get("status")));
    }

    // 🅲 Make captain – 200 OK
    @PatchMapping("/{id}/captain")
    public ProfileResponseDTO makeCaptain(@PathVariable Long id) {
        return profileMapper.toDTO(makeCaptain.make(id));
    }

    // 🅲 Make vice captain – 200 OK
    @PatchMapping("/{id}/vice-captain")
    public ProfileResponseDTO makeViceCaptain(@PathVariable Long id) {
        return profileMapper.toDTO(makeViceCaptain.make(id));
    }

    // ✅ Delete profile – 204 No Content
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        deleteProfile.delete(id);
    }
}
