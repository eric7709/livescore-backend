package com.livescore.app.matchEvents.utils;

import com.livescore.app.exceptions.BadRequestException;
import com.livescore.app.match.Match;
import com.livescore.app.match.enums.MatchPeriod;
import com.livescore.app.match.enums.MatchStatus;
import com.livescore.app.match.utils.MatchBroadcastService;
import com.livescore.app.matchEvents.MatchEvent;
import com.livescore.app.matchEvents.MatchEventRepository;
import com.livescore.app.matchEvents.dtos.MatchEventRequest;
import com.livescore.app.matchEvents.enums.EventType;
import com.livescore.app.matchLineup.LineupPlayer;
import com.livescore.app.matchLineup.MatchLineup;
import com.livescore.app.matchLineup.utils.LineupSupport;
import com.livescore.app.profile.Profile;
import com.livescore.app.profile.enums.Position;
import com.livescore.app.team.Team;
import com.livescore.app.utils.Resolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Holds all the request/entity-level helper logic used by {@code CreateMatchEvent}:
 * lineup resolution, event-rule validation, score/side-effect handling and match
 * clock calculation. CreateMatchEvent is left to orchestrate the flow only.
 */
@Component
@RequiredArgsConstructor
public class MatchEventSupport {

    private final MatchEventRepository matchEventRepository;
    private final Resolver resolver;
    private final LineupSupport lineupSupport;
    private final MatchBroadcastService matchBroadcastService;

    // -------------------------------------------------------------------------
    // Substitution request validation
    // -------------------------------------------------------------------------

    public void validateSubstitutionRequest(MatchEventRequest request) {
        if (request.getPrimaryPlayerId() == null) {
            throw new BadRequestException("Primary player (going OFF) is required for substitution");
        }
        if (request.getSecondaryPlayerId() == null) {
            throw new BadRequestException("Secondary player (coming ON) is required for substitution");
        }
        if (request.getPrimaryPlayerId().equals(request.getSecondaryPlayerId())) {
            throw new BadRequestException("Player going OFF and player coming ON cannot be the same");
        }

        Profile primaryPlayer = resolver.resolveProfile(request.getPrimaryPlayerId());
        Profile secondaryPlayer = resolver.resolveProfile(request.getSecondaryPlayerId());

        if (secondaryPlayer.getPosition().equals(Position.GK) && !primaryPlayer.getPosition().equals(Position.GK)) {
            throw new BadRequestException("Must Have a Goal Keeper on the pitch");
        }
    }

    // -------------------------------------------------------------------------
    // Lineup / player resolution
    // -------------------------------------------------------------------------

    public MatchLineup getTeamLineup(Match match, Team team) {
        if (match.getHomeTeam().getId().equals(team.getId())) {
            return match.getHomeLineup();
        }
        if (match.getAwayTeam().getId().equals(team.getId())) {
            return match.getAwayLineup();
        }
        throw new BadRequestException("Team is not participating in this match");
    }

    public Profile resolvePrimaryPlayer(
            MatchEventRequest request,
            Match match,
            MatchLineup matchLineup) {
        if (request.getEventType() == EventType.SAVE) {
            return resolveActiveGoalkeeper(matchLineup, match).getPlayer();
        }

        return request.getPrimaryPlayerId() == null
                ? null
                : resolver.resolveProfile(request.getPrimaryPlayerId());
    }

    public Profile resolveSecondaryPlayer(MatchEventRequest request) {
        return request.getSecondaryPlayerId() == null
                ? null
                : resolver.resolveProfile(request.getSecondaryPlayerId());
    }

    private LineupPlayer resolveActiveGoalkeeper(MatchLineup lineup, Match match) {
        if (lineup == null || lineup.getPlayers() == null || lineup.getPlayers().isEmpty()) {
            throw new BadRequestException("Lineup is unavailable while resolving the active goalkeeper");
        }

        List<MatchEvent> existingEvents = match.getEvents();

        List<LineupPlayer> activeGoalkeepers = lineup.getPlayers()
                .stream()
                .filter(player -> player.getPosition() == Position.GK)
                .filter(player -> lineupSupport.isPlayerOnPitch(
                        lineup.getPlayers(),
                        player.getPlayer().getId(),
                        existingEvents))
                .toList();

        if (activeGoalkeepers.isEmpty()) {
            throw new BadRequestException("No active goalkeeper is on the pitch for this team");
        }

        if (activeGoalkeepers.size() > 1) {
            throw new BadRequestException("Multiple active goalkeepers found; lineup data is inconsistent");
        }

        return activeGoalkeepers.get(0);
    }

    // -------------------------------------------------------------------------
    // Event rules
    // -------------------------------------------------------------------------

    public void validateEventRules(
            MatchEventRequest request,
            MatchLineup teamLineup,
            List<MatchEvent> existingEvents) {
        List<LineupPlayer> lineupPlayers = teamLineup.getPlayers();

        validatePlayerNotSentOff(request);
        validatePlayerOnPitchForScoring(request, existingEvents, lineupPlayers);
        validateAssistProvider(request, existingEvents, lineupPlayers);

        if (request.getEventType() == EventType.SUBSTITUTION) {
            validateSubstitution(request, existingEvents, lineupPlayers);
        }
    }

    private void validatePlayerNotSentOff(MatchEventRequest request) {
        boolean isCardEvent = request.getEventType() == EventType.YELLOW_CARD
                || request.getEventType() == EventType.RED_CARD
                || request.getEventType() == EventType.YELLOW_RED_CARD;

        if (!isCardEvent || request.getPrimaryPlayerId() == null) {
            return;
        }

        boolean alreadySentOff = matchEventRepository.countByMatchIdAndPrimaryPlayerIdAndEventTypeIn(
                request.getMatchId(),
                request.getPrimaryPlayerId(),
                List.of(EventType.RED_CARD, EventType.YELLOW_RED_CARD)) > 0;

        if (alreadySentOff) {
            throw new BadRequestException("Player has already been sent off in this match");
        }
    }

    private void validatePlayerOnPitchForScoring(
            MatchEventRequest request,
            List<MatchEvent> existingEvents,
            List<LineupPlayer> lineupPlayers) {
        boolean needsOnPitchPlayer = GOAL_EVENT_TYPES.contains(request.getEventType())
                || request.getEventType() == EventType.PENALTY_AWARDED
                || request.getEventType() == EventType.PENALTY_MISSED;

        if (!needsOnPitchPlayer || request.getPrimaryPlayerId() == null) {
            return;
        }

        if (lineupPlayers == null || lineupPlayers.isEmpty()) {
            throw new BadRequestException("Lineup is unavailable for player validation");
        }

        boolean onPitch = lineupSupport.isPlayerOnPitch(
                lineupPlayers,
                request.getPrimaryPlayerId(),
                existingEvents);

        if (!onPitch) {
            String action = GOAL_EVENT_TYPES.contains(request.getEventType())
                    ? "score a goal"
                    : "take part in this penalty event";
            throw new BadRequestException("Player must be on the pitch to " + action);
        }
    }

    private void validateAssistProvider(
            MatchEventRequest request,
            List<MatchEvent> existingEvents,
            List<LineupPlayer> lineupPlayers) {
        boolean isAssistableGoal = ASSISTABLE_GOAL_TYPES.contains(request.getEventType());

        if (!isAssistableGoal || request.getSecondaryPlayerId() == null) {
            return;
        }

        if (request.getSecondaryPlayerId().equals(request.getPrimaryPlayerId())) {
            throw new BadRequestException("A player cannot be credited with assisting their own goal");
        }

        if (lineupPlayers == null || lineupPlayers.isEmpty()) {
            throw new BadRequestException("Lineup is unavailable for assist validation");
        }

        boolean assisterOnPitch = lineupSupport.isPlayerOnPitch(
                lineupPlayers,
                request.getSecondaryPlayerId(),
                existingEvents);

        if (!assisterOnPitch) {
            throw new BadRequestException("Assisting player must be on the pitch to record an assist");
        }
    }

    private void validateSubstitution(
            MatchEventRequest request,
            List<MatchEvent> existingEvents,
            List<LineupPlayer> lineupPlayers) {
        Long playerGoingOffId = request.getPrimaryPlayerId();
        Long playerComingOnId = request.getSecondaryPlayerId();

        if (lineupPlayers == null || lineupPlayers.isEmpty()) {
            throw new BadRequestException("Lineup is unavailable for substitution validation");
        }

        if (!lineupSupport.isPlayerOnPitch(lineupPlayers, playerGoingOffId, existingEvents)) {
            throw new BadRequestException("Player going OFF is not on the pitch");
        }

        if (lineupSupport.isPlayerSubbedOff(existingEvents, playerGoingOffId)) {
            throw new BadRequestException("Player going OFF has already been substituted");
        }

        if (!lineupSupport.isPlayerAvailableToComeOn(
                lineupPlayers,
                playerComingOnId,
                existingEvents)) {
            throw new BadRequestException(
                    "Player coming ON must be an unused, eligible bench player");
        }
    }

    // -------------------------------------------------------------------------
    // Event side effects
    // -------------------------------------------------------------------------

    public void processEventSideEffects(
            MatchEventRequest request,
            Match match,
            MatchEvent event) {
        resolvePenaltyAward(request.getEventType(), match);

        if (GOAL_EVENT_TYPES.contains(request.getEventType())) {
            recordGoal(request, match);
        }

        applyCardUpgrade(event, request, match);
    }

    private void resolvePenaltyAward(EventType eventType, Match match) {
        boolean requiresPenaltyClearance = eventType == EventType.PENALTY_AWARDED
                || eventType == EventType.PENALTY_MISSED
                || GOAL_EVENT_TYPES.contains(eventType);

        if (!requiresPenaltyClearance) {
            return;
        }
        deleteOutstandingPenalties(match);
    }

    private void deleteOutstandingPenalties(Match match) {
        if (match.getEvents() == null || match.getEvents().isEmpty()) {
            return;
        }

        List<MatchEvent> outstandingAwards = match.getEvents().stream()
                .filter(el -> el.getEventType() == EventType.PENALTY_AWARDED)
                .toList();

        if (!outstandingAwards.isEmpty()) {
            matchEventRepository.deleteAll(outstandingAwards);
            match.getEvents().removeAll(outstandingAwards); // Evict from in-memory collection

            for (MatchEvent removed : outstandingAwards) {
                matchBroadcastService.broadcastEventDeleted(match.getId(), removed.getId());
            }
        }
    }

    private void recordGoal(MatchEventRequest request, Match match) {
        boolean eventTeamIsHome = request.getTeamId().equals(match.getHomeTeam().getId());
        boolean ownGoal = request.getEventType() == EventType.OWN_GOAL;

        if (ownGoal) {
            if (eventTeamIsHome) {
                match.setAwayScore(scoreOrZero(match.getAwayScore()) + 1);
            } else {
                match.setHomeScore(scoreOrZero(match.getHomeScore()) + 1);
            }
            return;
        }

        if (eventTeamIsHome) {
            match.setHomeScore(scoreOrZero(match.getHomeScore()) + 1);
        } else {
            match.setAwayScore(scoreOrZero(match.getAwayScore()) + 1);
        }
    }

    private void applyCardUpgrade(MatchEvent event, MatchEventRequest request, Match match) {
        if (request.getEventType() != EventType.YELLOW_CARD || request.getPrimaryPlayerId() == null) {
            event.setEventType(request.getEventType());
            return;
        }

        long existingYellowCards = match.getEvents().stream()
                .filter(el -> el.getPrimaryPlayer() != null
                        && el.getPrimaryPlayer().getId().equals(request.getPrimaryPlayerId())
                        && el.getEventType().equals(EventType.YELLOW_CARD))
                .count();

        event.setEventType(existingYellowCards >= 1
                ? EventType.YELLOW_RED_CARD
                : EventType.YELLOW_CARD);
    }

    private int scoreOrZero(Number score) {
        return score == null ? 0 : score.intValue();
    }

    // -------------------------------------------------------------------------
    // Match state and timing
    // -------------------------------------------------------------------------

    public void validateMatchIsLive(Match match) {
        if (match.getStatus() != MatchStatus.LIVE) {
            throw new BadRequestException(
                    "Cannot record events: match status is " + match.getStatus() + ", not LIVE");
        }

        if (!LIVE_PLAY_PERIODS.contains(match.getPeriod())) {
            throw new BadRequestException(
                    "Cannot record events: " + match.getPeriod() + " is not an active play period");
        }
    }

    public void validateRequestPeriodMatchesLivePeriod(MatchEventRequest request, Match match) {
        if (request.getPeriod() != match.getPeriod()) {
            throw new BadRequestException(
                    "Event period does not match the live match period. Refresh the match tracker and try again.");
        }
    }

    public void validateLineupsExist(Match match) {
        if (match.getHomeLineup() == null || match.getAwayLineup() == null) {
            throw new BadRequestException("Cannot create event because one or both lineups are missing");
        }
    }

    public int[] resolveMinuteAndSecond(Match match) {
        Instant periodStartedAt = match.getPeriodStartedAt();
        if (periodStartedAt == null) {
            throw new BadRequestException("Match has no periodStartedAt timestamp");
        }

        Integer baseMinute = PERIOD_BASE_MINUTE.get(match.getPeriod());
        if (baseMinute == null) {
            throw new BadRequestException("Cannot calculate time for period " + match.getPeriod());
        }

        Duration elapsed = Duration.between(periodStartedAt, Instant.now());
        if (elapsed.isNegative()) {
            elapsed = Duration.ZERO;
        }

        return new int[] {
                baseMinute + (int) elapsed.toMinutes(),
                (int) (elapsed.getSeconds() % 60)
        };
    }

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    public static final Set<EventType> GOAL_EVENT_TYPES = Set.of(
            EventType.GOAL,
            EventType.OWN_GOAL,
            EventType.PENALTY_GOAL,
            EventType.FREE_KICK_GOAL,
            EventType.LONG_RANGE_GOAL);

    public static final Set<EventType> ASSISTABLE_GOAL_TYPES = Set.of(
            EventType.GOAL,
            EventType.FREE_KICK_GOAL);

    private static final Map<MatchPeriod, Integer> PERIOD_BASE_MINUTE = Map.of(
            MatchPeriod.FIRST_HALF, 0,
            MatchPeriod.SECOND_HALF, 45,
            MatchPeriod.EXTRA_TIME_FIRST_HALF, 90,
            MatchPeriod.EXTRA_TIME_SECOND_HALF, 105,
            MatchPeriod.PENALTIES, 120);

    private static final Set<MatchPeriod> LIVE_PLAY_PERIODS = Set.of(
            MatchPeriod.FIRST_HALF,
            MatchPeriod.SECOND_HALF,
            MatchPeriod.EXTRA_TIME_FIRST_HALF,
            MatchPeriod.EXTRA_TIME_SECOND_HALF,
            MatchPeriod.PENALTIES);
}