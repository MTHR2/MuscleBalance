package com.musclebalance.dto.exercise;

import com.musclebalance.entity.MuscleRole;
import jakarta.validation.constraints.NotNull;

public record ExerciseMuscleRequestDto(@NotNull Short muscleId,
                                       @NotNull MuscleRole role) {
}
