package com.musclebalance.dto.workout;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AddWorkoutExerciseRequestDto(@NotNull Long exerciseId,
                                           @Positive @Max(50) Short targetSets) {
}
