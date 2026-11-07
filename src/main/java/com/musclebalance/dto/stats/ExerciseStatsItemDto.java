package com.musclebalance.dto.stats;

import com.musclebalance.entity.LoadType;

import java.math.BigDecimal;

public record ExerciseStatsItemDto(Long id,
                                   String name,
                                   String imageUrl,
                                   LoadType loadType,
                                   BigDecimal workingWeight) {
}
