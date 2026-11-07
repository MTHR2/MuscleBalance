package com.musclebalance.dto.workout;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AddSetRequestDto(
        @NotNull @Positive @Max(999) Short reps,
        @NotNull @PositiveOrZero @Digits(integer = 4, fraction = 2) BigDecimal weight,
        Boolean warmup
        ) {

}
