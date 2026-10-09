package com.livescore.app.competition.utils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;

import com.livescore.app.competition.Competition;
import com.livescore.app.competition.enums.CompetitionLegFormat;
import com.livescore.app.competition.enums.CompetitionScope;
import com.livescore.app.competition.enums.CompetitionStatus;
import com.livescore.app.competition.enums.CompetitionType;
import com.livescore.app.exceptions.BadRequestException;
import com.livescore.app.team.Team;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CompetitionUpdater {

    private final Competition competition;

    // -------------------------------------------------------------------------
    // Details
    // -------------------------------------------------------------------------

    public Competition updateName(String name) {
        if (name == null) {
            return competition;
        }
        String trimmedName = name.trim();
        if (trimmedName.isEmpty()) {
            throw new BadRequestException("Competition name cannot be empty.");
        }
        competition.setName(trimmedName);
        return competition;
    }

    public Competition updateCode(String code) {
        if (code == null) {
            return competition;
        }
        String trimmedCode = code.trim().toUpperCase();
        if (trimmedCode.isEmpty()) {
            throw new BadRequestException("Competition code cannot be empty.");
        }
        competition.setCompetitionCode(trimmedCode);
        return competition;
    }

    public Competition updateLogoUrl(String logoUrl) {
        if (logoUrl == null) {
            return competition;
        }
        competition.setLogoUrl(logoUrl.trim());
        return competition;
    }

    // -------------------------------------------------------------------------
    // Scope & Status
    // -------------------------------------------------------------------------

    public Competition updateScope(CompetitionScope scope) {
        if (scope == null) {
            return competition;
        }
        if (competition.getStatus() == CompetitionStatus.COMPLETED) {
            throw new BadRequestException("Cannot change the scope of a completed competition.");
        }
        competition.setScope(scope);
        return competition;
    }

    public Competition updateStatus(CompetitionStatus status) {
        if (status == null) {
            return competition;
        }

        CompetitionStatus currentStatus = competition.getStatus();

        if (currentStatus == CompetitionStatus.COMPLETED && status == CompetitionStatus.ONGOING) {
            throw new BadRequestException("Cannot set a completed competition back to in-progress.");
        }

        if (status == CompetitionStatus.ONGOING 
                && (competition.getTeams() == null || competition.getTeams().isEmpty())) {
            throw new BadRequestException("Cannot start a competition with no teams assigned.");
        }

        competition.setStatus(status);
        return competition;
    }

    // -------------------------------------------------------------------------
    // Dates
    // -------------------------------------------------------------------------

    public Competition updateStartDate(LocalDateTime startDate) {
        if (startDate == null) {
            return competition;
        }
        if (competition.getEndDate() != null && competition.getEndDate().isBefore(startDate)) {
            throw new BadRequestException("Start date cannot be after end date.");
        }
        if (competition.getStatus() == CompetitionStatus.ONGOING && startDate.isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Start date cannot be set in the future for an in-progress competition.");
        }
        competition.setStartDate(startDate);
        return competition;
    }

    public Competition updateEndDate(LocalDateTime endDate) {
        if (endDate == null) {
            return competition;
        }
        if (competition.getStartDate() != null && endDate.isBefore(competition.getStartDate())) {
            throw new BadRequestException("End date cannot be before start date.");
        }
        competition.setEndDate(endDate);
        return competition;
    }

    // -------------------------------------------------------------------------
    // Format Options
    // -------------------------------------------------------------------------

    public Competition updateType(CompetitionType type) {
        if (type == null) {
            return competition;
        }
        validateFormatEditable();
        competition.setCompetitionType(type);
        return competition;
    }

    public Competition updateLegFormat(CompetitionLegFormat legFormat) {
        if (legFormat == null) {
            return competition;
        }
        validateFormatEditable();
        competition.setLegFormat(legFormat);
        return competition;
    }

    public Competition updateTotalRounds(Integer totalRounds) {
        if (totalRounds == null) {
            return competition;
        }
        if (totalRounds <= 0) {
            throw new BadRequestException("Total rounds must be greater than 0.");
        }
        validateFormatEditable();
        competition.setTotalRounds(totalRounds);
        return competition;
    }

    // -------------------------------------------------------------------------
    // Team Management
    // -------------------------------------------------------------------------

    /**
     * Replaces all existing teams with a new unique set.
     */
    public Competition updateTeams(Set<Team> teams) {
        if (teams == null) {
            return competition;
        }
        validateTeamModification();
        competition.replaceTeams(teams);
        return competition;
    }

    /**
     * Adds teams to the current competition without duplicates.
     */
    public Competition addTeams(Collection<Team> newTeams) {
        if (newTeams == null || newTeams.isEmpty()) {
            return competition;
        }
        validateTeamModification();
        competition.addTeams(newTeams);
        return competition;
    }

    /**
     * Adds a single team without duplicates.
     */
    public Competition addTeam(Team team) {
        if (team == null) {
            return competition;
        }
        validateTeamModification();
        competition.addTeam(team);
        return competition;
    }

    // -------------------------------------------------------------------------
    // Private Helpers
    // -------------------------------------------------------------------------

    private void validateFormatEditable() {
        if (competition.getStatus() != CompetitionStatus.SCHEDULED) {
            throw new BadRequestException("Format settings can only be modified when competition status is SCHEDULED.");
        }
    }

    private void validateTeamModification() {
        if (competition.getStatus() == CompetitionStatus.ONGOING 
                || competition.getStatus() == CompetitionStatus.COMPLETED) {
            throw new BadRequestException("Cannot modify teams once the competition has started or finished.");
        }
    }
}