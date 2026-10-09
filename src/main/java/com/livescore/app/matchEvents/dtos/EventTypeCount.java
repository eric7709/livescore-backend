package com.livescore.app.matchEvents.dtos;

import com.livescore.app.matchEvents.enums.EventType;

public record EventTypeCount(EventType eventType, long homeValue, long awayValue) {}