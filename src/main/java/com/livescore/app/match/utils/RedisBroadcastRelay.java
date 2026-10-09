package com.livescore.app.match.utils;

import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisBroadcastRelay implements MessageListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            MatchBroadcastService.RedisEnvelope envelope =
                    objectMapper.readValue(message.getBody(), MatchBroadcastService.RedisEnvelope.class);
            messagingTemplate.convertAndSend(envelope.destination(), envelope.payloadJson());
        } catch (Exception e) {
            log.error("Failed to relay Redis broadcast to STOMP", e);
        }
    }
}