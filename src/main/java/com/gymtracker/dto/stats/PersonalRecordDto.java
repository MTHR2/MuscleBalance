package com.gymtracker.dto.stats;

import com.gymtracker.entity.LoadType;

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
