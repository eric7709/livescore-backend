package com.livescore.app.competition.utils;

import com.livescore.app.competition.Competition;
import com.livescore.app.competition.enums.CompetitionStatus;
import com.livescore.app.exceptions.BadRequestException;

public final class FixtureGenerationGuard {

    private FixtureGenerationGuard() {
    }

    public static void assertCanGenerate(Competition competition, boolean hasMatches) {
        if (competition.getStatus() != CompetitionStatus.SCHEDULED) {
            throw new BadRequestException(
                    "Fixtures can only be generated while the competition is SCHEDULED (it is "
                            + competition.getStatus() + ")");
        }
        if (hasMatches) {
            throw new BadRequestException("Fixtures have already been generated for this competition");
        }
    }
}
