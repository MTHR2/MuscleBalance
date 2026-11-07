package com.musclebalance.dto.plan;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PlanDayRequestDto(@NotNull Long workoutTemplateId,
                                @NotNull @Min(1) @Max(7) Short dayOfWeek) {
}
