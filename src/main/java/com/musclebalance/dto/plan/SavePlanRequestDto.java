package com.musclebalance.dto.plan;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SavePlanRequestDto(@NotBlank @Size(max = 100) String name,
                                  @Size(max = 500) String description,
                                  @Valid @NotEmpty @Size(max = 7) List<PlanDayRequestDto> days,
                                 @Positive @Max(100) Short defaultWeeklySets,
                                 @Valid @Size(max = 30) List<MuscleTargetRequestDto> muscleTargets ) {
}
