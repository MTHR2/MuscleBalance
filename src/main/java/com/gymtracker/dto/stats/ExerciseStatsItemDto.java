package com.gymtracker.dto.stats;

import com.gymtracker.entity.LoadType;

import java.math.BigDecimal;

public record ExerciseStatsItemDto(Long id,
                                   String name,
                                   String imageUrl,
                                   LoadType loadType,
                                   BigDecimal workingWeight) {
}
