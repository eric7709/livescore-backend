package com.zestio.app.match.helpers;

import tools.jackson.databind.ObjectMapper;
import com.zestio.app.match.Match;
import com.zestio.app.match.dto.MatchDTO;
import com.zestio.app.matchLineup.dtos.MatchLineupDTO;
import com.zestio.app.matchLineup.MatchLineup;
import com.zestio.app.matchEvents.dtos.MatchEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchBroadcastService {

    public static final String REDIS_CHANNEL = "ws-broadcast";
    public static final String LIST_TOPIC = "/topic/matches"; // card grids subscribe here
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    public void broadcastLineup(MatchLineup lineup) {
        publish(
                lineupTopic(lineup.getMatch().getId(), lineup.getTeam().getId()),
                MatchLineupDTO.mapToDTO(lineup));
    }
    
    private static String lineupTopic(Long matchId, Long teamId) {
           return "/topic/matches/" + matchId + "/lineup/" + teamId;
       }
    

    

    /**
     * Publishes the full current match state (score, status, period, etc).
     * Call whenever score, status, or period changes — never on cosmetic
     * field edits (stadium name, match type, etc).
     */
    public void broadcastState(Match match) {
        MatchDTO dto = MatchDTO.fromEntity(match);
        publish(stateTopic(match.getId()), dto);
        publish(LIST_TOPIC, dto); // lets any "matches for date/all" list know to refetch
    }

    /** Publishes a newly created match event to live viewers. */
    public void broadcastEventCreated(Long matchId, MatchEventDTO event) {
        publish(eventsTopic(matchId), event);
    }
    /**
     * Publishes that a previously-broadcast event has been removed — e.g. an
     * outstanding PENALTY_AWARDED cleared once the penalty is scored/missed,
     * or an explicit delete. Clients should reconcile their local event list
     * against this rather than assuming every broadcast is an addition.
     */
    public void broadcastEventDeleted(Long matchId, Long eventId) {
        publish(eventsTopic(matchId), new EventDeleted(eventId));
    }

    private void publish(String destination, Object payload) {
        redisTemplate.convertAndSend(REDIS_CHANNEL, new RedisEnvelope(destination, toJson(payload)));
    }

    @SneakyThrows
    private String toJson(Object payload) {
        return objectMapper.writeValueAsString(payload);
    }

    private static String stateTopic(Long matchId) {
        return "/topic/matches/" + matchId + "/state";
    }

    private static String eventsTopic(Long matchId) {
        return "/topic/matches/" + matchId + "/events";
    }

    public record EventDeleted(Long id, boolean deleted) {
        public EventDeleted(Long id) {
            this(id, true);
        }
    }

    /** What actually travels over Redis: which "room" (destination) + the pre-packaged message (JSON). */
    public record RedisEnvelope(String destination, String payloadJson) {}
}