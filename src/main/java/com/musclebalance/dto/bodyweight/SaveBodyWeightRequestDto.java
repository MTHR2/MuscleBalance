package com.musclebalance.dto.bodyweight;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SaveBodyWeightRequestDto(@PastOrPresent LocalDate date,
                                       @NotNull @DecimalMin("20.00") @DecimalMax("500.00") @Digits(integer = 3, fraction = 2)
                                       BigDecimal weight) {
}
