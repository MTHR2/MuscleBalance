package com.musclebalance.dto.stats;

import com.musclebalance.entity.LoadType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PersonalRecordDto(Long exerciseId,
                                String name,
                                LoadType loadType,
                                LocalDate date,
                                BigDecimal weight,
                                short reps,
                                BigDecimal delta) {
}
