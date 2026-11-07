package com.musclebalance.dto.workout;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpdateSetRequestDto(
        @NotNull @Positive @Max(999) Short reps,
        @NotNull @PositiveOrZero @Digits(integer = 4, fraction = 2) BigDecimal weight,
        @NotNull Boolean warmup
) {
}
