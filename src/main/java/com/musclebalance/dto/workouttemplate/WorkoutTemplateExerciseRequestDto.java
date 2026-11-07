package com.musclebalance.dto.workouttemplate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WorkoutTemplateExerciseRequestDto(
        @NotNull Long exerciseId,
        @NotNull @Positive Short targetSets,
        @Positive Short repsMin,
        @Positive Short repsMax
) {
}
