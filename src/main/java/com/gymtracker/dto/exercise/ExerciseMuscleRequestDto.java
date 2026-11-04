package com.gymtracker.dto.exercise;

import com.gymtracker.entity.MuscleRole;
import jakarta.validation.constraints.NotNull;

public record ExerciseMuscleRequestDto(@NotNull Short muscleId,
                                       @NotNull MuscleRole role) {
}
