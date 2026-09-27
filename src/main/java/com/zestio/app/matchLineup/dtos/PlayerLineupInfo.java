package com.zestio.app.matchLineup.dtos;

import com.zestio.app.matchLineup.enums.BookingStatus;
import com.zestio.app.matchLineup.enums.LineupStatus;
import com.zestio.app.matchLineup.enums.SubstitutionStatus;
import com.zestio.app.profile.enums.Position;

import lombok.Data;

@Data
public class PlayerLineupInfo {
    private Long playerId;
    private String playerName;
    private Integer squadNumber;
    private Position position;
    private BookingStatus bookingStatus;
    private LineupStatus lineupStatus;
    private SubstitutionStatus substitutionStatus;
    private boolean isSubbable;
    private boolean isBookable;
    private boolean isAbleToScoreOrAssist;
    private boolean isAbleToComeOn;
}