package com.gymtracker.dto.workout;

import java.time.OffsetDateTime;
import java.util.List;

public record WorkoutDetailsDto(Long id,
                                String name,
                                OffsetDateTime startedAt,
                                OffsetDateTime finishedAt,
                                List<WorkoutExerciseDto> exercises) {
}
