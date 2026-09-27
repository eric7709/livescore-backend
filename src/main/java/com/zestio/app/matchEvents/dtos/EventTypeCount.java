package com.zestio.app.matchEvents.dtos;

import com.zestio.app.matchEvents.enums.EventType;

public record EventTypeCount(EventType eventType, long homeValue, long awayValue) {}