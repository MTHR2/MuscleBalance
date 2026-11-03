package com.gymtracker.dto.workout;

import java.time.OffsetDateTime;

public record WorkoutSummaryDto(
        Long id,
        String name,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        long durationSeconds,
        Integer completionPercent,
        int completedSets
) {
}
