package com.gymtracker.dto.bodyweight;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BodyWeightDto(Long id,
                            LocalDate date,
                            BigDecimal weight) {
}
