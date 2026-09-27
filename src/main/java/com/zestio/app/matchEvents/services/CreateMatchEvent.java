package com.zestio.app.matchEvents.services;

import com.zestio.app.exceptions.BadRequestException;
import com.zestio.app.match.Match;
import com.zestio.app.match.helpers.MatchBroadcastService;
import com.zestio.app.matchEvents.MatchEvent;
import com.zestio.app.matchEvents.MatchEventRepository;
import com.zestio.app.matchEvents.dtos.MatchEventDTO;
import com.zestio.app.matchEvents.dtos.MatchEventRequest;
import com.zestio.app.matchEvents.enums.EventType;
import com.zestio.app.matchEvents.helper.MatchEventSupport;
import com.zestio.app.matchLineup.MatchLineup;
import com.zestio.app.profile.Profile;
import com.zestio.app.team.Team;
import com.zestio.app.utils.Resolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateMatchEvent {
    private final MatchEventRepository matchEventRepository;
    private final Resolver resolver;
    private final MatchEventSupport matchEventSupport;
    private final MatchBroadcastService matchBroadcastService;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public MatchEventDTO create(MatchEventRequest request) {
        validateRequestPayload(request);

        MatchEvent event = new MatchEvent();
        Match match = mapRequestToEvent(request, event);

        MatchEventDTO dto = MatchEventDTO.fromEntity(matchEventRepository.save(event));

        matchBroadcastService.broadcastEventCreated(request.getMatchId(), dto);

        if (MatchEventSupport.GOAL_EVENT_TYPES.contains(request.getEventType())) {
            matchBroadcastService.broadcastState(match);
        }

        return dto;
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------
    private void validateRequestPayload(MatchEventRequest request) {
        if (request.getMatchId() == null) {
            throw new BadRequestException("matchId is required");
        }
        if (request.getTeamId() == null) {
            throw new BadRequestException("teamId is required");
        }
        if (request.getEventType() == null) {
            throw new BadRequestException("eventType is required");
        }
        if (request.getPeriod() == null) {
            throw new BadRequestException("period is required");
        }

        if (request.getEventType() == EventType.SUBSTITUTION) {
            matchEventSupport.validateSubstitutionRequest(request);
        }

        if (MatchEventSupport.GOAL_EVENT_TYPES.contains(request.getEventType())
                && request.getPrimaryPlayerId() == null) {
            throw new BadRequestException("primaryPlayerId is required for goal events");
        }

        if (request.getEventType() == EventType.OWN_GOAL && request.getSecondaryPlayerId() != null) {
            throw new BadRequestException("Own goals cannot be credited with an assist");
        }

        if (!MatchEventSupport.ASSISTABLE_GOAL_TYPES.contains(request.getEventType())
                && request.getSecondaryPlayerId() != null
                && MatchEventSupport.GOAL_EVENT_TYPES.contains(request.getEventType())) {
            throw new BadRequestException("This goal type cannot be credited with an assist");
        }
    }

    // -------------------------------------------------------------------------
    // Orchestration
    // -------------------------------------------------------------------------
    private Match mapRequestToEvent(MatchEventRequest request, MatchEvent event) {
        Match match = resolver.resolveMatch(request.getMatchId());
        Team team = resolver.resolveTeam(request.getTeamId());
        matchEventSupport.validateMatchIsLive(match);
        matchEventSupport.validateRequestPeriodMatchesLivePeriod(request, match);
        matchEventSupport.validateLineupsExist(match);

        MatchLineup teamLineup = matchEventSupport.getTeamLineup(match, team);
        List<MatchEvent> existingEvents = match.getEvents();

        Profile primaryPlayer = matchEventSupport.resolvePrimaryPlayer(request, match, teamLineup);
        Profile secondaryPlayer = matchEventSupport.resolveSecondaryPlayer(request);

        matchEventSupport.validateEventRules(request, teamLineup, existingEvents);

        event.setMatch(match);
        event.setTeam(team);
        event.setPeriod(match.getPeriod());
        event.setEventData(request.getEventData());
        event.setPrimaryPlayer(primaryPlayer);
        event.setSecondaryPlayer(secondaryPlayer);

        matchEventSupport.processEventSideEffects(request, match, event);

        int[] matchClock = matchEventSupport.resolveMinuteAndSecond(match);
        event.setMinute(matchClock[0]);
        event.setSecond(matchClock[1]);
        return match;
    }
}