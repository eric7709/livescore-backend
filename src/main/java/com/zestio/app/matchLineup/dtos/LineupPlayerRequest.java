package com.zestio.app.matchLineup.dtos;

import com.zestio.app.matchLineup.enums.LineupStatus;
import com.zestio.app.matchLineup.enums.MissingReason;
import com.zestio.app.profile.enums.Position;

import lombok.Data;

@Data
public class LineupPlayerRequest {
    private Long playerId;
    private Position position;
    private String slotLabel; // exact formation slot, e.g. "LCM", "RCM", "GK" — nullable for legacy data
    private LineupStatus status;
    private MissingReason missingReason;
}