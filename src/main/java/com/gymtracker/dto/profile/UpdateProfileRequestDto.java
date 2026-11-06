package com.gymtracker.dto.profile;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

public record UpdateProfileRequestDto(@DecimalMin("20.00") @DecimalMax("500.00") @Digits(integer = 3, fraction = 2)
                                      BigDecimal targetBodyWeight) {
}
