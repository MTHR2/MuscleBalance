package com.gymtracker.dto.stats;

import com.gymtracker.entity.LoadType;

import java.math.BigDecimal;
import java.util.List;

public record ExerciseProgressDto(Long exerciseId,
                                  String name,
                                  LoadType loadType,
                                  List<ProgressPointDto> points,
                                  BigDecimal current,
                                  BigDecimal delta,
                                  Integer deltaPercent,
                                  BigDecimal estimatedMax) {
}
