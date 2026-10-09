package com.livescore.app.matchLineup.dtos;

import com.livescore.app.matchLineup.enums.LineupStatus;
import com.livescore.app.matchLineup.enums.MissingReason;
import com.livescore.app.profile.enums.Position;

import lombok.Data;

@Data
public class LineupPlayerRequest {
    private Long playerId;
    private Position position;
    private String slotLabel; // exact formation slot, e.g. "LCM", "RCM", "GK" — nullable for legacy data
    private LineupStatus status;
    private MissingReason missingReason;
}