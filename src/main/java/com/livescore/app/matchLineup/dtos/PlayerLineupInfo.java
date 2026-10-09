package com.livescore.app.matchLineup.dtos;

import com.livescore.app.matchLineup.enums.BookingStatus;
import com.livescore.app.matchLineup.enums.LineupStatus;
import com.livescore.app.matchLineup.enums.SubstitutionStatus;
import com.livescore.app.profile.enums.Position;

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