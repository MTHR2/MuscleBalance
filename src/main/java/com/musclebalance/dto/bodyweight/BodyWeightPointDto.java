package com.musclebalance.dto.bodyweight;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BodyWeightPointDto(Long id,
                                 LocalDate date,
                                 BigDecimal weight,
                                 BigDecimal avg7) {
}
