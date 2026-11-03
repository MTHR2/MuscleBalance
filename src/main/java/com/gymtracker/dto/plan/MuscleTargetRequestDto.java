package com.gymtracker.dto.plan;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record MuscleTargetRequestDto(@NotNull Short muscleId,
                                     @NotNull @PositiveOrZero @Max(100) Short weeklySets) {
}
