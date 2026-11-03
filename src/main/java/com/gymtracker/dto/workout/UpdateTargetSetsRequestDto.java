package com.gymtracker.dto.workout;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateTargetSetsRequestDto(
        @NotNull @PositiveOrZero @Max(50) Short targetSets) {
}
