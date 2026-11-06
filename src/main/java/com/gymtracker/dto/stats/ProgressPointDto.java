package com.gymtracker.dto.stats;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProgressPointDto(LocalDate weekStart,
                               BigDecimal weight,
                               short reps,
                               boolean record) {
}
