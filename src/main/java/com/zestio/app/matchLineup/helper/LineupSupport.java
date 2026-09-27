package com.zestio.app.matchLineup.helper;

import com.zestio.app.exceptions.BadRequestException;
import com.zestio.app.exceptions.NotFoundException;
import com.zestio.app.match.Match;
import com.zestio.app.match.enums.MatchStatus;
import com.zestio.app.matchEvents.MatchEvent;
import com.zestio.app.matchEvents.enums.EventType;
import com.zestio.app.matchLineup.LineupPlayer;
import com.zestio.app.matchLineup.LineupPlayerRepository;
import com.zestio.app.matchLineup.MatchLineup;
import com.zestio.app.matchLineup.MatchLineupRepository;
import com.zestio.app.matchLineup.dtos.LineupPlayerRequest;
import com.zestio.app.matchLineup.dtos.MatchLineUpRequest;
import com.zestio.app.matchLineup.dtos.PlayerLineupInfo;
import com.zestio.app.matchLineup.enums.BookingStatus;
import com.zestio.app.matchLineup.enums.LineupStatus;
import com.zestio.app.matchLineup.enums.SubstitutionStatus;
import com.zestio.app.profile.Profile;
import com.zestio.app.team.Team;
import com.zestio.app.utils.Resolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Shared logic used by more than one lineup operation: validation rules,
 * player-stats/eligibility calculation, and lineup lookup. Plays the same
 * role for the matchLineup.services classes that Resolver plays for the
 * team.services classes — not an operation itself, just what the
 * Get/Submit/Update classes (and CreateMatchEvent) depend on.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LineupSupport {

    private final LineupPlayerRepository lineupPlayerRepository;
    private final MatchLineupRepository matchLineupRepository;
    private final Resolver resolver;

    // -------------------------------------------------------------------------
    // Lineup lookup
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public MatchLineup resolveTeamLineup(Long matchId, Long teamId) {
        return matchLineupRepository.findByMatchIdAndTeamId(matchId, teamId)
                .orElseThrow(() -> new NotFoundException("No lineup exists for this team in this match"));
    }

    // -------------------------------------------------------------------------
    // Submit/update validation and persistence
    // -------------------------------------------------------------------------

    public void applyLineupRequest(MatchLineup lineup, MatchLineUpRequest request) {
        Profile captain = resolver.resolveProfile(request.getCaptainId());

        lineup.setCaptain(captain);
        lineup.setFormation(request.getFormation());
    }

    public void validateLineupRequestIds(MatchLineUpRequest request) {
        if (request == null) {
            throw new BadRequestException("Lineup request is required");
        }
        if (request.getMatchId() == null) {
            throw new BadRequestException("matchId is required");
        }
        if (request.getTeamId() == null) {
            throw new BadRequestException("teamId is required");
        }
        if (request.getCaptainId() == null || request.getCaptainId() <= 0) {
            throw new BadRequestException("A valid captainId is required");
        }
        if (request.getFormation() == null) {
            throw new BadRequestException("formation is required");
        }
        if (request.getPlayers() == null || request.getPlayers().isEmpty()) {
            throw new BadRequestException("At least one lineup player is required");
        }
    }

    public void validateLineupPlayers(MatchLineUpRequest request, Team team) {
        Set<Long> playerIds = new HashSet<>();
        int starterCount = 0;
        boolean captainIsStarter = false;

        for (LineupPlayerRequest playerRequest : request.getPlayers()) {
            if (playerRequest.getPlayerId() == null) {
                throw new BadRequestException("Each lineup player must have a playerId");
            }
            if (playerRequest.getStatus() == null) {
                throw new BadRequestException("Each lineup player must have a status");
            }
            if (!playerIds.add(playerRequest.getPlayerId())) {
                throw new BadRequestException("A player cannot appear more than once in a lineup");
            }

            verifyPlayerInTeam(team, playerRequest.getPlayerId());

            if (playerRequest.getStatus() == LineupStatus.STARTER) {
                starterCount++;
                if (playerRequest.getPlayerId().equals(request.getCaptainId())) {
                    captainIsStarter = true;
                }
            }
        }

        if (starterCount != 11) {
            throw new BadRequestException("A lineup must contain exactly 11 starters");
        }
        if (!captainIsStarter) {
            throw new BadRequestException("The captain must be part of the starting eleven");
        }
    }

    public List<LineupPlayer> saveLineupPlayers(
            List<LineupPlayerRequest> requests,
            MatchLineup lineup) {
        List<LineupPlayer> players = new ArrayList<>();

        for (LineupPlayerRequest request : requests) {
            Profile player = resolver.resolveProfile(request.getPlayerId());

            LineupPlayer lineupPlayer = new LineupPlayer();
            lineupPlayer.setMatchLineup(lineup);
            lineupPlayer.setPlayer(player);
            lineupPlayer.setPosition(request.getPosition());
            lineupPlayer.setSlotLabel(request.getSlotLabel());
            lineupPlayer.setStatus(request.getStatus());
            lineupPlayer.setMissingReason(request.getMissingReason());

            players.add(lineupPlayerRepository.save(lineupPlayer));
        }

        return players;
    }

    public void verifyPlayerInTeam(Team team, Long playerId) {
        boolean belongsToTeam = team.getPlayers()
                .stream()
                .anyMatch(player -> player.getId().equals(playerId));

        if (!belongsToTeam) {
            throw new BadRequestException("Player is not part of this team");
        }
    }

    public void verifyTeamParticipatesInMatch(Match match, Team team) {
        boolean participates = match.getHomeTeam().getId().equals(team.getId())
                || match.getAwayTeam().getId().equals(team.getId());

        if (!participates) {
            throw new BadRequestException("Team is not participating in this match");
        }
    }

    public void verifyMatchIsValidForLineup(Match match) {
        if (match.getStatus() != MatchStatus.SCHEDULED) {
            throw new BadRequestException(
                    "Lineups can only be created or modified while the match is SCHEDULED");
        }
    }

    // -------------------------------------------------------------------------
    // Player state calculation
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<PlayerLineupInfo> buildPlayerStats(
            MatchLineup lineup,
            List<MatchEvent> events) {
        List<PlayerLineupInfo> stats = new ArrayList<>();

        for (LineupPlayer lineupPlayer : lineup.getPlayers()) {
            stats.add(toPlayerLineupInfo(
                    lineupPlayer,
                    lineup.getTeam(),
                    lineup.getPlayers(),
                    events));
        }

        // A manager is not necessarily one of Team.players or lineup.players,
        // but can still receive a booking. Add them once when not already in
        // the lineup. If they are also a named lineup player, their existing
        // entry is marked bookable by toPlayerLineupInfo instead.
        appendManagerBookingInfo(stats, lineup, events);

        return stats;
    }

    private PlayerLineupInfo toPlayerLineupInfo(
            LineupPlayer lineupPlayer,
            Team team,
            List<LineupPlayer> lineupPlayers,
            List<MatchEvent> events) {
        Long playerId = lineupPlayer.getPlayer().getId();
        Profile player = lineupPlayer.getPlayer();
        boolean isManager = isTeamManager(team, playerId);

        PlayerLineupInfo info = new PlayerLineupInfo();
        info.setPlayerId(playerId);
        info.setPlayerName(player.getFullName());
        info.setPosition(player.getPosition());
        info.setSquadNumber(player.getSquadNumber());
        info.setLineupStatus(lineupPlayer.getStatus());
        info.setBookingStatus(getBookingStatus(events, playerId));
        info.setSubstitutionStatus(getSubstitutionStatus(events, playerId));
        info.setSubbable(isPlayerSubbable(lineupPlayers, playerId, events));
        info.setBookable(isManager
                ? !isPlayerSentOff(events, playerId)
                : isPlayerBookable(lineupPlayers, playerId, events));
        info.setAbleToScoreOrAssist(isPlayerAbleToScoreOrAssist(lineupPlayers, playerId, events));
        info.setAbleToComeOn(isPlayerAvailableToComeOn(lineupPlayers, playerId, events));

        return info;
    }

    private void appendManagerBookingInfo(
            List<PlayerLineupInfo> stats,
            MatchLineup lineup,
            List<MatchEvent> events) {
        Profile manager = lineup.getTeam().getManager();
        if (manager == null) {
            return;
        }

        boolean managerAlreadyListed = lineup.getPlayers()
                .stream()
                .anyMatch(player -> player.getPlayer().getId().equals(manager.getId()));
        if (managerAlreadyListed) {
            return;
        }

        PlayerLineupInfo managerInfo = new PlayerLineupInfo();
        managerInfo.setPlayerId(manager.getId());
        managerInfo.setPlayerName(manager.getFullName());
        managerInfo.setPosition(manager.getPosition());
        managerInfo.setSquadNumber(manager.getSquadNumber());
        managerInfo.setLineupStatus(null);
        managerInfo.setBookingStatus(getBookingStatus(events, manager.getId()));
        managerInfo.setSubstitutionStatus(null);
        managerInfo.setSubbable(false);
        managerInfo.setAbleToComeOn(false);
        managerInfo.setAbleToScoreOrAssist(false);
        managerInfo.setBookable(!isPlayerSentOff(events, manager.getId()));

        stats.add(managerInfo);
    }

    private boolean isTeamManager(Team team, Long profileId) {
        return team.getManager() != null
                && team.getManager().getId().equals(profileId);
    }

    // -------------------------------------------------------------------------
    // Substitution contract
    // -------------------------------------------------------------------------
    // For every SUBSTITUTION event throughout the backend:
    // primaryPlayer = player going OFF
    // secondaryPlayer = player coming ON
    // -------------------------------------------------------------------------

    private boolean isPrimary(MatchEvent event, Long playerId) {
        return event.getPrimaryPlayer() != null
                && event.getPrimaryPlayer().getId().equals(playerId);
    }

    private boolean isSecondary(MatchEvent event, Long playerId) {
        return event.getSecondaryPlayer() != null
                && event.getSecondaryPlayer().getId().equals(playerId);
    }

    /** The incoming substitute is stored as secondaryPlayer. */
    private boolean isPlayerSubbedOn(List<MatchEvent> events, Long playerId) {
        return events.stream()
                .anyMatch(event -> event.getEventType() == EventType.SUBSTITUTION
                        && isSecondary(event, playerId));
    }

    /** The outgoing player is stored as primaryPlayer. */
    public boolean isPlayerSubbedOff(List<MatchEvent> events, Long playerId) {
        return events.stream()
                .anyMatch(event -> event.getEventType() == EventType.SUBSTITUTION
                        && isPrimary(event, playerId));
    }

    private boolean isPlayerStarter(List<LineupPlayer> lineupPlayers, Long playerId) {
        return lineupPlayers.stream()
                .anyMatch(player -> player.getStatus() == LineupStatus.STARTER
                        && player.getPlayer().getId().equals(playerId));
    }

    private boolean isPlayerSubstitute(List<LineupPlayer> lineupPlayers, Long playerId) {
        return lineupPlayers.stream()
                .anyMatch(player -> player.getStatus() == LineupStatus.SUBSTITUTE
                        && player.getPlayer().getId().equals(playerId));
    }

    public boolean isPlayerOnPitch(
            List<LineupPlayer> lineupPlayers,
            Long playerId,
            List<MatchEvent> events) {
        if (playerId == null) {
            return false;
        }

        boolean beganOrEnteredPlay = isPlayerStarter(lineupPlayers, playerId)
                || isPlayerSubbedOn(events, playerId);

        return beganOrEnteredPlay
                && !isPlayerSubbedOff(events, playerId)
                && !isPlayerSentOff(events, playerId);
    }

    /**
     * A player can enter only once, must have been named on the BENCH, and
     * cannot be on the pitch or sent off already.
     */
    public boolean isPlayerAvailableToComeOn(
            List<LineupPlayer> lineupPlayers,
            Long playerId,
            List<MatchEvent> events) {
        return playerId != null
                && isPlayerSubstitute(lineupPlayers, playerId)
                && !isPlayerSubbedOn(events, playerId)
                && !isPlayerOnPitch(lineupPlayers, playerId, events)
                && !isPlayerSentOff(events, playerId);
    }

    private boolean isPlayerSubbable(
            List<LineupPlayer> lineupPlayers,
            Long playerId,
            List<MatchEvent> events) {
        return isPlayerOnPitch(lineupPlayers, playerId, events);
    }

    private boolean isPlayerAbleToScoreOrAssist(
            List<LineupPlayer> lineupPlayers,
            Long playerId,
            List<MatchEvent> events) {
        return isPlayerOnPitch(lineupPlayers, playerId, events);
    }

    private boolean isPlayerBookable(
            List<LineupPlayer> lineupPlayers,
            Long playerId,
            List<MatchEvent> events) {
        return isPlayerOnPitch(lineupPlayers, playerId, events)
                && !isPlayerSentOff(events, playerId);
    }

    private BookingStatus getBookingStatus(List<MatchEvent> events, Long playerId) {
        if (isPlayerOnYellowRed(events, playerId)) {
            return BookingStatus.YELLOW_RED_CARD;
        }
        if (isPlayerOnRed(events, playerId)) {
            return BookingStatus.RED_CARD;
        }
        if (isPlayerOnYellow(events, playerId)) {
            return BookingStatus.YELLOW_CARD;
        }
        return null;
    }

    private SubstitutionStatus getSubstitutionStatus(List<MatchEvent> events, Long playerId) {
        if (isPlayerSubbedOff(events, playerId)) {
            return SubstitutionStatus.SUBBED_OFF;
        }
        if (isPlayerSubbedOn(events, playerId)) {
            return SubstitutionStatus.SUBBED_ON;
        }
        return null;
    }

    private boolean isPlayerOnYellow(List<MatchEvent> events, Long playerId) {
        return events.stream().anyMatch(event -> event.getEventType() == EventType.YELLOW_CARD
                && isPrimary(event, playerId));
    }

    private boolean isPlayerOnYellowRed(List<MatchEvent> events, Long playerId) {
        return events.stream().anyMatch(event -> event.getEventType() == EventType.YELLOW_RED_CARD
                && isPrimary(event, playerId));
    }

    private boolean isPlayerOnRed(List<MatchEvent> events, Long playerId) {
        return events.stream().anyMatch(event -> event.getEventType() == EventType.RED_CARD
                && isPrimary(event, playerId));
    }

    private boolean isPlayerSentOff(List<MatchEvent> events, Long playerId) {
        return isPlayerOnYellowRed(events, playerId) || isPlayerOnRed(events, playerId);
    }
}